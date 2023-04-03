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

import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.internal.config.OpenHABSerialBridgeConfig;
import org.connectorio.addons.binding.wmbus.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.binding.wmbus.internal.transport.WMBusMessageListenerAdapter;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusSerialBuilder;

public class WMBusJrxtxSerialBridgeHandler extends WMBusSerialBridgeBaseHandler<OpenHABSerialBridgeConfig> {

  public WMBusJrxtxSerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge, serialPortManager, discoveryCoordinator);
  }

  @Override
  protected CompletableFuture<WMBusConnection> initialize(WMBusMessageDispatcher dispatcher) {
    CompletableFuture<WMBusConnection> connection = new CompletableFuture<>();
    scheduler.execute(new Runnable() {
      @Override
      public void run() {
        OpenHABSerialBridgeConfig config = getConfigAs(OpenHABSerialBridgeConfig.class);
        WMBusMessageListenerAdapter listener = new WMBusMessageListenerAdapter(dispatcher, (message) -> {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        });
        WMBusSerialBuilder builder = new WMBusSerialBuilder(config.manufacturer, listener, config.serialPort);
        builder.setMode(config.mode);
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
