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
package org.connectorio.addons.binding.mbus.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.mbus.config.SerialBridgeConfig;
import org.connectorio.addons.binding.mbus.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.binding.mbus.internal.transport.serial.SerialTransportBuilder;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openmuc.jmbus.MBusAdapterConnectionBuilder;
import org.openmuc.jmbus.MBusConnection;

public class MBusSerialBridgeHandler extends MBusBridgeBaseHandler<SerialBridgeConfig> implements MBusBridgeHandler<SerialBridgeConfig> {

  private final SerialPortManager serialPortManager;
  private final DiscoveryCoordinator discoveryCoordinator;

  public MBusSerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge, discoveryCoordinator);

    this.serialPortManager = serialPortManager;
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  protected CompletableFuture<MBusConnection> initializeConnection() {
    CompletableFuture<MBusConnection> connection = new CompletableFuture<>();
    SerialBridgeConfig config = getConfigAs(SerialBridgeConfig.class);

    if (config.serialPort == null || config.serialPort.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port must be set");
      return connection;
    }

    scheduler.execute(new Runnable() {
      @Override
      public void run() {
        SerialBridgeConfig config = getConfigAs(SerialBridgeConfig.class);
        SerialTransportBuilder builder = new SerialTransportBuilder(serialPortManager, config.serialPort, new MBusAdapterConnectionBuilder());
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

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public Long getRefreshInterval() {
    return 0L;
  }

}
