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
package org.connectorio.addons.binding.wmbus.internal.handler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.internal.config.SerialBridgeConfig;
import org.connectorio.addons.binding.wmbus.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.binding.wmbus.internal.transport.WMBusMessageListenerAdapter;
import org.connectorio.addons.binding.wmbus.internal.transport.serial.SerialTransportBuilder;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openmuc.jmbus.wireless.WMBusConnection;

public class WMBusSerialBridgeHandler extends WMBusBridgeBaseHandler<SerialBridgeConfig> {

  private final SerialPortManager serialPortManager;

  public WMBusSerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge, discoveryCoordinator);
    this.serialPortManager = serialPortManager;
  }

  @Override
  protected CompletableFuture<WMBusConnection> initializeConnection(WMBusMessageDispatcher dispatcher,
      Consumer<IOException> reconnect) {
    CompletableFuture<WMBusConnection> connection = new CompletableFuture<>();
    SerialBridgeConfig config = getConfigAs(SerialBridgeConfig.class);

    if (config.serialPort == null || config.serialPort.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port must be set");
      return connection;
    }
    if (config.manufacturer == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Manufacturer must be set");
      return connection;
    }
    if (config.mode == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Mode must be set");
      return connection;
    }

    scheduler.execute(new Runnable() {
      @Override
      public void run() {
        SerialBridgeConfig config = getConfigAs(SerialBridgeConfig.class);
        WMBusMessageListenerAdapter listener = new WMBusMessageListenerAdapter(dispatcher, reconnect);
        SerialTransportBuilder builder = new SerialTransportBuilder(serialPortManager, config.manufacturer, listener, config.serialPort);
        builder.setMode(config.mode);
        builder.setSerialPortConfig(config);
        try {
          connection.complete(builder.build());
        } catch (Exception e) {
          connection.completeExceptionally(e);
        }
      }
    });
    return connection;
  }

}
