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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusSerialBuilder;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.StopBits;

public class WMBusSerialJrxtxBridgeHandler extends WMBusBridgeBaseHandler<SerialBridgeConfig> {

  public WMBusSerialJrxtxBridgeHandler(Bridge bridge, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge, discoveryCoordinator);
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
        WMBusSerialBuilder builder = new WMBusSerialBuilder(
          config.manufacturer, listener, config.serialPort
        );
        builder.setMode(config.mode);
        if (config.baudRate != null) {
          builder.setBaudrate(config.baudRate);
        }
        if (config.dataBits != null) {
          builder.setDataBits(dataBits(config.dataBits));
        }
        if (config.stopBits != null) {
          builder.setStopBits(stopBits(config.stopBits));
        }
        try {
          connection.complete(builder.build());
        } catch (Exception e) {
          connection.completeExceptionally(e);
        }
      }
    });
    return connection;
  }

  private StopBits stopBits(org.connectorio.addons.io.transport.serial.config.StopBits stopBits) {
    switch (stopBits) {
      case STOPBITS_1:
        return StopBits.STOPBITS_1;
      case STOPBITS_2:
        return StopBits.STOPBITS_2;
      case STOPBITS_1_5:
        return StopBits.STOPBITS_1_5;
    }
    throw new IllegalArgumentException("Unsupported stop bit setting: " + stopBits);
  }

  private DataBits dataBits(org.connectorio.addons.io.transport.serial.config.DataBits dataBits) {
    switch (dataBits) {
      case DATABITS_5:
        return DataBits.DATABITS_5;
      case DATABITS_6:
        return DataBits.DATABITS_6;
      case DATABITS_7:
        return DataBits.DATABITS_7;
      case DATABITS_8:
        return DataBits.DATABITS_8;
    }
    throw new IllegalArgumentException("Unsupported data bit setting: " + dataBits);
  }

}
