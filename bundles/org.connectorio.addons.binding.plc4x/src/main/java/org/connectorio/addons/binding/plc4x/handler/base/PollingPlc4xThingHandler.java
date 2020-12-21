/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.handler.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.connectorio.addons.binding.config.PollingConfiguration;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.connectorio.addons.binding.plc4x.handler.Plc4xBridgeHandler;
import org.connectorio.addons.binding.plc4x.handler.Plc4xThingHandler;
import org.connectorio.addons.binding.plc4x.handler.task.WriteTask;
import org.connectorio.addons.binding.plc4x.config.CommonChannelConfiguration;
import org.connectorio.addons.binding.plc4x.handler.task.ReadTask;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PollingPlc4xThingHandler<T extends PlcConnection, B extends Plc4xBridgeHandler<T, ?>, C extends PollingConfiguration> extends
  BasePollingThingHandler<B, C> implements Plc4xThingHandler<T, B, C> {

  protected final Map<ChannelUID, ScheduledFuture> futures = new ConcurrentHashMap<>();
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private T connection;

  public PollingPlc4xThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    getBridgeHandler().map(Plc4xBridgeHandler::getConnection)
      .map(future -> future.whenCompleteAsync(this::connect, this.scheduler));
  }

  private void connect(T connection, Throwable e) {
    if (e != null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
      return;
    }

    List<String> configErrors = new ArrayList<>();
    for (Channel channel : thing.getChannels()) {
      final ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
      if (channelTypeUID == null) {
        logger.warn("Channel {} has no type", channel.getLabel());
        continue;
      }
      final CommonChannelConfiguration channelConfig = channel.getConfiguration().as(
        CommonChannelConfiguration.class);

      try {
        Long cycleTime = channelConfig.refreshInterval == null ? getRefreshInterval() : channelConfig.refreshInterval;
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new ReadTask(connection, getCallback(), channel), 0,
          cycleTime, TimeUnit.MILLISECONDS);
        futures.put(channel.getUID(), future);
      } catch (PlcRuntimeException er) {
        logger.warn("Channel configuration error", er);
        configErrors.add(channel.getLabel() + ": " + er.getMessage());
      }
    }

    // If some channels could not start up, put the entire thing offline and display the channels
    // in question to the user.
    if (!configErrors.isEmpty()) {
      clearTasks();
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        "Errors in field configuration: "
          + configErrors.stream().collect(Collectors.joining(",")));
      return;
    }

    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    Channel channel = getThing().getChannel(channelUID);

    if (RefreshType.REFRESH == command) {
      getBridgeConnection().ifPresent(connection -> scheduler.submit(new ReadTask(connection,
          getCallback(), channel)));
    } else {
      getBridgeConnection()
          .ifPresent(connection -> scheduler.submit(new WriteTask(connection, channel, command)));
    }
  }

  protected Optional<T> getBridgeConnection() {
    return getBridgeHandler().map(Plc4xBridgeHandler::getConnection).map(CompletableFuture::join);
  }

  @Override
  public void dispose() {
    // clean up tasks which should not remain in scheduler pool
    clearTasks();
  }

  private void clearTasks() {
    futures.forEach((k, v) -> v.cancel(false));
  }

}
