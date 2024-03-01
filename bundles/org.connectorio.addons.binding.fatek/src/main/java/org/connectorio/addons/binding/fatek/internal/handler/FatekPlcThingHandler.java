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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.binding.fatek.config.BridgeConfig;
import org.connectorio.addons.binding.fatek.config.DeviceConfig;
import org.connectorio.addons.binding.fatek.internal.channel.FatekChannelHandler;
import org.connectorio.addons.binding.fatek.internal.channel.FatekChannelHandlerFactory;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.binding.handler.PollingHandler;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekReadMixDataCmd;
import org.simplify4u.jfatek.FatekWriteDataCmd;
import org.simplify4u.jfatek.FatekWriteDiscreteCmd;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.DisReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FatekPlcThingHandler extends BasePollingThingHandler<FatekBridgeHandler<BridgeConfig>, DeviceConfig>
  implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(FatekPlcThingHandler.class);
  private final FatekChannelHandlerFactory channelHandlerFactory;
  private final Map<ChannelUID, FatekChannelHandler> handlerMap = new ConcurrentHashMap<>();
  private final Map<Reg, FatekChannelHandler> registerMap = new ConcurrentHashMap<>();
  private FaconConnection connection;
  private Integer stationNumber;
  private ScheduledFuture<?> future;

  public FatekPlcThingHandler(Thing thing, FatekChannelHandlerFactory channelHandlerFactory) {
    super(thing);
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

      for (Channel channel : getThing().getChannels()) {
        FatekChannelHandler handler = channelHandlerFactory.create(channel);
        if (handler != null) {
          handlerMap.put(channel.getUID(), handler);
          registerMap.put(handler.register(), handler);
        }
      }
      Long interval = getThingConfig().map(cfg -> cfg.refreshInterval)
        .orElseGet(() -> getBridgeHandler().map(PollingHandler::getRefreshInterval)
          .orElseGet(this::getDefaultPollingInterval));
      future = scheduler.scheduleAtFixedRate(this, interval, interval, TimeUnit.MILLISECONDS);

      updateStatus(ThingStatus.ONLINE);
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (handlerMap.containsKey(channelUID)) {
      FatekChannelHandler handler = handlerMap.get(channelUID);
      Reg register = handler.register();
      FatekCommand<?> fatekCmd = null; //handler.prepareWrite(command);
      if (fatekCmd == null) {
        logger.warn("Could not map command {} from channel {} to value supported by PLC. Ignoring this write.", command, channelUID);
        return;
      }

      write(channelUID, command, fatekCmd);
    }
  }

  private void write(ChannelUID channel, Command command, FatekCommand<?> cmd) {
    connection.execute(stationNumber, cmd).whenComplete((r, e) -> {
      if (e != null) {
        logger.error("Could not signal channel {} with command {} by fatek command {}", channel, command, cmd, e);
        return;
      }
      logger.debug("Successful write of channel {} with command {} annd fatek command {}", channel, command, cmd);
      update(channel, command);
    });
  }

  private void update(ChannelUID channelUID, Type value) {
    if (value instanceof State) {
      getCallback().stateUpdated(channelUID, (State) value);
    }
  }

  @Override
  public void dispose() {
    if (future != null) {
      future.cancel(false);
    }
    super.dispose();
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

  @Override
  public void run() {
    connection.execute(stationNumber, new FatekReadMixDataCmd(null, registerMap.keySet())).whenComplete((r, e) -> {
      if (e != null) {
        logger.warn("Failed to retrieve data", e);
        return;
      }
      for (Entry<Reg, RegValue> entry : r.entrySet()) {
        FatekChannelHandler handler = registerMap.get(entry.getKey());
        State state = handler.state(entry.getValue());
        if (state != null) {
          update(handler.channel(), state);
        }
      }
    });
  }
}
