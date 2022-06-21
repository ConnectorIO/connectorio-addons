/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.internal.handler;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.spi.values.PlcStruct;
import org.connectorio.addons.binding.plc4x.canopen.CANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.api.CoConnection;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.config.CoNodeBridgeConfig;
import org.connectorio.addons.binding.plc4x.canopen.config.CoSdoConfig;
import org.connectorio.addons.binding.plc4x.canopen.handler.CoBridgeHandler;
import org.connectorio.addons.binding.plc4x.canopen.config.CoNodeConfig;
import org.connectorio.addons.binding.plc4x.canopen.handler.HeartbeatMonitor;
import org.connectorio.addons.binding.plc4x.canopen.handler.ThingStatusHeartbeatCallback;
import org.connectorio.addons.binding.plc4x.canopen.internal.provider.CoSdoChannelTypeProvider;
import org.connectorio.addons.binding.plc4x.handler.Plc4xBridgeHandler;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoNodeBridgeHandler extends PollingPlc4xBridgeHandler<PlcConnection, CoNodeBridgeConfig> {

  private final Logger logger = LoggerFactory.getLogger(CoNodeBridgeHandler.class);

  protected final Map<ChannelUID, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

  private CompletableFuture<CoNode> node = new CompletableFuture<>();
  private CoNode coNode;
  private CoNodeBridgeConfig config;
  private ScheduledFuture<?> heartbeatMonitorTask;

  public CoNodeBridgeHandler(Bridge thing) {
    super(thing);
  }

  @Override
  protected CompletableFuture<PlcConnection> getPlcConnection() {
    return CompletableFuture.supplyAsync(() -> getBridgeHandler()
      .map(Plc4xBridgeHandler::getConnection)
      .orElseThrow(() -> new IllegalStateException("Not ready")).join()
    );
  }

  @Override
  public void initialize() {
    this.config = getConfigAs(CoNodeBridgeConfig.class);

    getPlcConnection().thenAccept(connection -> {
      CompletableFuture<CoConnection> network = getBridgeHandler().get().getCoConnection();
      network.whenComplete((nw, error) -> {
        if (error != null) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
          return;
        }

        this.coNode = nw.getNode(config.nodeId);

        for (Channel channel : getThing().getChannels()) {
          final CoSdoConfig config = channel.getConfiguration().as(CoSdoConfig.class);
          CANOpenDataType type = CoSdoChannelTypeProvider.typeFromChannel(channel.getChannelTypeUID());

          Long refreshInterval = Optional.ofNullable(config.refreshInterval)
            .orElse(getRefreshInterval());

          futures.put(channel.getUID(), scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
              coNode.read((short) config.index, (short) config.subIndex, type)
                .whenComplete((result, error) -> {
                  if (error != null) {
                    logger.warn("Failed to retrieve {}/{} (0x{}/0x{}) configured in channel {}", config.index, config.subIndex,
                      Integer.toHexString(config.index).toUpperCase(), Integer.toHexString(config.subIndex).toUpperCase(),
                      channel.getUID(), error);
                    return;
                  }
                  update(channel, type, result);
                });
            }
          }, 0, refreshInterval, TimeUnit.MILLISECONDS));
        }

        this.node.complete(this.coNode);

        ThingStatusHeartbeatCallback callback = new ThingStatusHeartbeatCallback(thing, getCallback());
        HeartbeatMonitor heartbeatMonitor = new HeartbeatMonitor(nw, coNode, callback, System.currentTimeMillis(), config.heartbeatTimeout);
        heartbeatMonitorTask = scheduler.scheduleAtFixedRate(heartbeatMonitor, 1000, config.heartbeatTimeout, TimeUnit.MILLISECONDS);

        updateStatus(ThingStatus.ONLINE);
      });
    });
  }

  private void update(Channel channel, CANOpenDataType type, Object result) {
    if (result instanceof Double) {
      getCallback().stateUpdated(channel.getUID(), new DecimalType((Double) result));
    } else if (result instanceof BigDecimal) {
      getCallback().stateUpdated(channel.getUID(), new DecimalType((BigDecimal) result));
    } else if (result instanceof Long) {
      getCallback().stateUpdated(channel.getUID(), new DecimalType((Long) result));
    } else if (result instanceof Integer) {
      getCallback().stateUpdated(channel.getUID(), new DecimalType((Integer) result));
    } else if (result instanceof Short) {
      getCallback().stateUpdated(channel.getUID(), new DecimalType((Short) result));
    } else if (result instanceof String) {
      getCallback().stateUpdated(channel.getUID(), new StringType((String) result));
    } else {
      logger.warn("Unsupported result {} (representation of CANopen {}). Channel {} will not be updated", result, type, channel);
    }
  }

  public CompletableFuture<CoNode> getNode() {
    return node;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return CANopenBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  public Optional<CoBridgeHandler<?>> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(CoBridgeHandler.class::isInstance)
      .map(handler -> (CoBridgeHandler<?>) handler);
  }

  @Override
  public void dispose() {
    clearTasks();
    if (heartbeatMonitorTask != null) {
      if (!heartbeatMonitorTask.isDone()) {
        heartbeatMonitorTask.cancel(true);
        heartbeatMonitorTask = null;
      }
    }
    if (coNode != null) {
      coNode.close();
      coNode = null;
    }
  }

  private void clearTasks() {
    futures.forEach((k, v) -> v.cancel(false));
  }
}
