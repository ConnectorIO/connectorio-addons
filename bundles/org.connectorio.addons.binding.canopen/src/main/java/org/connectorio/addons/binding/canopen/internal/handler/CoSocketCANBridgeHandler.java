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
package org.connectorio.addons.binding.canopen.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.measure.Quantity;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.canopen.tag.CANOpenTag;
import org.apache.plc4x.java.spi.connection.AbstractPlcConnection;
import org.connectorio.addons.binding.can.statistic.CANStatisticCollector;
import org.connectorio.addons.binding.canopen.api.CoConnection;
import org.connectorio.addons.binding.canopen.discovery.CoDiscoveryParticipant;
import org.connectorio.addons.binding.canopen.handler.CoBridgeHandler;
import org.connectorio.addons.binding.canopen.internal.config.DiscoveryMode;
import org.connectorio.addons.binding.canopen.internal.config.SocketCANConfiguration;
import org.connectorio.addons.binding.canopen.internal.discovery.CoNetworkDiscoveryService;
import org.connectorio.addons.binding.canopen.internal.plc4x.DefaultConnection;
import org.connectorio.addons.binding.canopen.internal.statistics.SocketCANStatisticCollectors;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xBridgeHandler;
import org.connectorio.plc4x.extras.osgi.PlcDriverManager;
import org.connectorio.plc4x.extras.decorator.CompositeDecorator;
import org.connectorio.plc4x.extras.decorator.Decorator;
import org.connectorio.plc4x.extras.decorator.retry.RetryDecorator;
import org.connectorio.plc4x.extras.decorator.throttle.ThrottleDecorator;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoSocketCANBridgeHandler extends PollingPlc4xBridgeHandler<CANOpenTag, SocketCANConfiguration>
  implements CoBridgeHandler<SocketCANConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(CoSocketCANBridgeHandler.class);
  private final PlcDriverManager driverManager;
  private List<CANStatisticCollector> collectors;
  private CompletableFuture<PlcConnection> initializer = new CompletableFuture<>();
  private List<CoDiscoveryParticipant> participants;
  private SocketCANConfiguration config;
  private ScheduledFuture<?> statisticPoller;

  public CoSocketCANBridgeHandler(Bridge thing, PlcDriverManager driverManager, List<CoDiscoveryParticipant> participants) {
    super(thing);
    this.driverManager = driverManager;
    this.participants = participants;
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.UNKNOWN);

    Runnable connectionTask = new Runnable() {
      @Override
      public void run() {
        try {
          config = getBridgeConfig().get();
          String nodeId = "nodeId=" + config.nodeId;
          String heartbeat = "&heartbeat=" + config.heartbeat;
          AbstractPlcConnection connection = (AbstractPlcConnection) driverManager
            .getConnectionManager().getConnection("canopen:socketcan://" + config.name + "?" + nodeId + heartbeat);

          if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            initializer.complete(connection);
            collectors = SocketCANStatisticCollectors.create(config.name);
            statisticPoller = scheduler.scheduleAtFixedRate(new Runnable() {
              @Override
              public void run() {
                fetchStatistics();
              }
            }, 0, 1, TimeUnit.MINUTES);
          } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection failed");
            initializer.complete(null);
          }
        } catch (PlcRuntimeException e) {
          logger.error("Connection attempt failed due to runtime error", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
          initializer.completeExceptionally(e);
        } catch (PlcConnectionException e) {
          logger.warn("Could not obtain connection", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
          initializer.completeExceptionally(e);
        }
      }
    };
    scheduler.submit(connectionTask);
  }

  private void fetchStatistics() {
    if (collectors == null || collectors.isEmpty() || getCallback() == null) {
      return;
    }

    for (CANStatisticCollector collector : collectors) {
      Quantity<?> statistic = collector.getStatistic();
      if (statistic.getUnit().equals(Units.ONE)) {
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), collector.getName()), new DecimalType(statistic.getValue().doubleValue()));
      } else {
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), collector.getName()), new QuantityType<>(statistic.getValue().doubleValue(), statistic.getUnit()));
      }
    }
  }

  @Override
  public void dispose() {
    if (statisticPoller != null) {
      statisticPoller.cancel(true);
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  protected CompletableFuture<PlcConnection> getPlcConnection() {
    return initializer;
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    DiscoveryMode discoveryMode = getBridgeConfig().map(config -> config.discoveryMode).orElse(DiscoveryMode.NMT_LISTEN);
    switch (discoveryMode) {
      case NONE:
        break;
      case NMT_LISTEN:
        return Collections.singleton(CoNetworkDiscoveryService.class);
      case SDO_SCAN:
        logger.warn("SDO scan discovery not implemented");
    }

    return Collections.emptyList();
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

  @Override
  public String getName() {
    return getBridgeConfig().map(cfg -> cfg.name).orElse("");
  }

  @Override
  public int getNodeId() {
    return getBridgeConfig().map(cfg -> cfg.nodeId).orElseThrow(() -> new IllegalArgumentException("Bridge node id (client/server id) is not set"));
  }

  @Override
  public List<CoDiscoveryParticipant> getParticipants() {
    return participants;
  }

  @Override
  public CompletableFuture<CoConnection> getCoConnection(Decorator... decorators) {
    return getPlcConnection().thenApply(connection -> {
      final CompositeDecorator composition = composeDecorators(decorators);
      return new DefaultConnection(getNodeId(), connection, composition);
    });
  }

  private CompositeDecorator composeDecorators(Decorator[] decorators) {
    CompositeDecorator composition = new CompositeDecorator();
    for (Decorator extension : decorators) {
      composition.add(extension);
    }
    if (config.retryCount > 0) {
      composition.add(new RetryDecorator(config.retryCount));
    }
    if (config.throttleReading > 0 || config.throttleWriting > 0) {
      composition.add(new ThrottleDecorator(
        config.throttleReading > 0 ? config.throttleReading : Long.MAX_VALUE,
        config.throttleWriting > 0 ? config.throttleWriting : Long.MAX_VALUE
      ));
    }
    return composition;
  }

}
