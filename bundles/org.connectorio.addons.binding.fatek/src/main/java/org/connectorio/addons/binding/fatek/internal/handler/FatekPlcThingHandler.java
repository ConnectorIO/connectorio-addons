/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.fatek.internal.handler;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.simplify4u.jfatek.io.FatekIOException;
import org.connectorio.addons.binding.config.PollingConfiguration;
import org.connectorio.addons.binding.fatek.config.BridgeConfig;
import org.connectorio.addons.binding.fatek.config.DeviceConfig;
import org.connectorio.addons.binding.fatek.internal.channel.FatekChannelHandler;
import org.connectorio.addons.binding.fatek.internal.channel.FatekChannelHandlerFactory;
import org.connectorio.addons.binding.fatek.internal.handler.source.FatekSamplerComposer;
import org.connectorio.addons.binding.fatek.internal.handler.source.FatekRegisterSampler;
import org.connectorio.addons.binding.fatek.internal.handler.source.FatekSampler;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.connectorio.addons.binding.source.SourceFactory;
import org.connectorio.addons.binding.source.sampling.SamplingSource;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FatekPlcThingHandler extends BasePollingThingHandler<FatekBridgeHandler<BridgeConfig>, DeviceConfig> {

  private final Logger logger = LoggerFactory.getLogger(FatekPlcThingHandler.class);
  private final SourceFactory sourceFactory;
  private final FatekChannelHandlerFactory channelHandlerFactory;
  private final Map<ChannelUID, FatekChannelHandler> handlerMap = new ConcurrentHashMap<>();
  private FaconConnection connection;
  private Integer stationNumber;
  private SamplingSource<FatekSampler> source;

  public FatekPlcThingHandler(Thing thing, SourceFactory sourceFactory, FatekChannelHandlerFactory channelHandlerFactory) {
    super(thing);
    this.sourceFactory = sourceFactory;
    this.channelHandlerFactory = channelHandlerFactory;
  }

  @Override
  public void initialize() {
    CompletableFuture<FaconConnection> bridgeConnection = getBridgeHandler()
      .map(FatekBridgeHandler::getConnection)
      .orElse(null);

    if (bridgeConnection == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Could not find bridge");
      return;
    }

    DeviceConfig config = getConfigAs(DeviceConfig.class);
    if (config.stationNumber == 0) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid station number");
      return;
    }
    this.stationNumber = config.stationNumber;
    bridgeConnection.whenComplete((result, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge supplied connection is not available");
        return;
      }
      this.connection = result;

      this.source = sourceFactory.sampling(scheduler, new FatekSamplerComposer(connection, stationNumber));
      StringBuilder validation = new StringBuilder();
      for (Channel channel : getThing().getChannels()) {
        FatekChannelHandler handler = channelHandlerFactory.create(channel);
        if (handler != null) {String validationMsg = handler.validateConfiguration();
          if (validationMsg != null && !validationMsg.isEmpty()) {
            validation.append(validationMsg);
          }
          Long refreshInterval = Optional.ofNullable(channel.getConfiguration().as(PollingConfiguration.class))
            .map(cfg -> cfg.refreshInterval)
            .filter(interval -> interval != 0)
            .orElseGet(this::getRefreshInterval);
          source.add(refreshInterval, channel.getUID().getAsString(), new FatekRegisterSampler(connection, stationNumber, handler.registers(),
              new HandlerCallback(handler, this::update, this::signalConnectionError)));
          handlerMap.put(channel.getUID(), handler);
        }
      }

      if (validation.length() != 0) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, validation.toString());
        return;
      }
      source.start();

      updateStatus(ThingStatus.ONLINE);
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (handlerMap.containsKey(channelUID)) {
      FatekChannelHandler handler = handlerMap.get(channelUID);
      if (RefreshType.REFRESH.equals(command) && this.source != null) {
        // read channel status independently of scheduled tasks
        source.request(new FatekRegisterSampler(connection, stationNumber, handler.registers(),
            new HandlerCallback(handler, this::update, this::signalConnectionError)));
        return;
      }
      FatekCommand<?> cmd = handler.prepareWrite(command);
      if (cmd == null) {
        logger.warn("Could not map command {}({}) from channel {} to value supported by PLC. Ignoring this write.",
            command, command.getClass(), channelUID);
        return;
      }

      write(channelUID, command, cmd);
    }
  }

  private void write(ChannelUID channel, Command command, FatekCommand<?> cmd) {
    connection.execute(stationNumber, cmd).whenComplete((r, e) -> {
      if (e != null) {
        logger.error("Could not signal channel {} with command {} by fatek command {}", channel, command, cmd, e);
        signalConnectionError(e);
        return;
      }
      logger.debug("Successful write of channel {} with command {} and fatek command {}", channel, command, cmd);
    });
  }

  private void signalConnectionError(Throwable cause) {
    if (cause instanceof FatekIOException || cause instanceof TimeoutException
        || (cause.getCause() instanceof FatekIOException)) {
      logger.warn("Communication failure on station {}, signalling bridge", stationNumber, cause);
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause.getMessage());
      getBridgeHandler().map(FatekBridgeHandler::getConnectionErrorHandler)
          .ifPresent(handler -> handler.accept(cause instanceof Exception ? (Exception) cause : new RuntimeException(cause)));
    } else {
      logger.warn("Non-IO error on station {}", stationNumber, cause);
    }
  }

  private void update(ChannelUID channelUID, Type value) {
    if (value instanceof State) {
      getCallback().stateUpdated(channelUID, (State) value);
    }
  }

  @Override
  public void dispose() {
    if (source != null) {
      source.stop();
    }
    super.dispose();
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

  static class HandlerCallback implements BiConsumer<Map<Reg, RegValue>, Throwable> {

    private final FatekChannelHandler handler;
    private final BiConsumer<ChannelUID, State> callback;
    private final Consumer<Throwable> errorCallback;

    public HandlerCallback(FatekChannelHandler handler, BiConsumer<ChannelUID, State> callback, Consumer<Throwable> errorCallback) {
      this.handler = handler;
      this.callback = callback;
      this.errorCallback = errorCallback;
    }

    @Override
    public void accept(Map<Reg, RegValue> regValues, Throwable error) {
      if (error != null) {
        errorCallback.accept(error);
        return;
      }
      State state = handler.state(new ArrayList<>(regValues.values()));
      if (state != null) {
        callback.accept(handler.channel(), state);
      }
    }
  }
}
