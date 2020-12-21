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
package org.connectorio.addons.binding.plc4x.canopen.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.spi.connection.AbstractPlcConnection;
import org.connectorio.addons.binding.plc4x.canopen.handler.CANopenBridgeHandler;
import org.connectorio.addons.binding.plc4x.canopen.internal.discovery.CANopenNMTDiscoveryService;
import org.connectorio.addons.binding.plc4x.canopen.discovery.CANopenDiscoveryParticipant;
import org.connectorio.addons.binding.plc4x.canopen.internal.config.DiscoveryMode;
import org.connectorio.addons.binding.plc4x.canopen.internal.config.SocketCANConfiguration;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xBridgeHandler;
import org.connectorio.addons.binding.plc4x.osgi.PlcDriverManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CANOpenSocketCANBridgeHandler extends PollingPlc4xBridgeHandler<PlcConnection, SocketCANConfiguration>
  implements CANopenBridgeHandler<SocketCANConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(CANOpenSocketCANBridgeHandler.class);
  private final PlcDriverManager driverManager;
  private CompletableFuture<PlcConnection> initializer = new CompletableFuture<>();
  private List<CANopenDiscoveryParticipant> participants;

  public CANOpenSocketCANBridgeHandler(Bridge thing, PlcDriverManager driverManager, List<CANopenDiscoveryParticipant> participants) {
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
          SocketCANConfiguration config = getBridgeConfig().get();
          String nodeId = "nodeId=" + config.nodeId;
          String heartbeat = "&heartbeat=" + config.heartbeat;
          AbstractPlcConnection connection = (AbstractPlcConnection) driverManager
            .getConnection("canopen:javacan://" + config.name + "?" + nodeId + heartbeat);

          if (!connection.isConnected()) {
            connection.connect();
          }

          if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            initializer.complete(connection);
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
      case NMT_LISTEN:
        return Collections.singleton(CANopenNMTDiscoveryService.class);
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
  public List<CANopenDiscoveryParticipant> getParticipants() {
    return participants;
  }

}
