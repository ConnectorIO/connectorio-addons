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
package org.connectorio.binding.plc4x.siemens.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.spi.connection.AbstractPlcConnection;
import org.connectorio.binding.plc4x.shared.handler.base.PollingPlc4xBridgeHandler;
import org.connectorio.binding.plc4x.shared.osgi.PlcDriverManager;
import org.connectorio.binding.plc4x.siemens.internal.SiemensBindingConstants;
import org.connectorio.binding.plc4x.siemens.internal.config.SiemensNetworkConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensNetworkBridgeHandler} is responsible for handling communication with Siemens S7 PLCs over network
 * sockets.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class SiemensNetworkBridgeHandler extends PollingPlc4xBridgeHandler<PlcConnection, SiemensNetworkConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(SiemensNetworkBridgeHandler.class);
  private final PlcDriverManager driverManager;

  private CompletableFuture<PlcConnection> initializer = new CompletableFuture<>();

  public SiemensNetworkBridgeHandler(Bridge thing, PlcDriverManager driverManager) {
    super(thing);
    this.driverManager = driverManager;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.UNKNOWN);

    Runnable connectionTask = new Runnable() {
      @Override
      public void run() {
        try {
          SiemensNetworkConfiguration config = getBridgeConfig().get();
          String local = "local-slot=" + config.localSlot + "&local-rack=" + config.localRack;
          String remote = "&remote-slot=" + config.remoteSlot + "&remote-rack=" + config.remoteRack;
          String pdu = config.pduSize != null ? "&pdu-size=" + config.pduSize : "";
          String type = config.controllerType != null ? "&controller-type=" + config.controllerType.getType().name() : "";
          PlcConnection connection = driverManager
            .getConnection("s7://" + config.host + "?" + local + remote + pdu + type);

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
  public CompletableFuture<PlcConnection> getPlcConnection() {
    return initializer;
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return SiemensBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
