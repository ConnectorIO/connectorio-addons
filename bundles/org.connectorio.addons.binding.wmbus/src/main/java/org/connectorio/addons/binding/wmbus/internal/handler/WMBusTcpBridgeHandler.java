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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.internal.config.TcpBridgeConfig;
import org.connectorio.addons.binding.wmbus.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.binding.wmbus.internal.transport.WMBusMessageListenerAdapter;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusConnection.WMBusTcpBuilder;

public class WMBusTcpBridgeHandler extends WMBusBridgeBaseHandler<TcpBridgeConfig> {

  public WMBusTcpBridgeHandler(Bridge bridge, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge, discoveryCoordinator);
  }

  @Override
  protected CompletableFuture<WMBusConnection> initializeConnection(WMBusMessageDispatcher dispatcher, Consumer<IOException> reconnect) {
    CompletableFuture<WMBusConnection> connection = new CompletableFuture<>();
    scheduler.execute(new Runnable() {
      @Override
      public void run() {
        TcpBridgeConfig config = getConfigAs(TcpBridgeConfig.class);
        if (config.manufacturer == null) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing manufacturer");
          return;
        }
        if (config.hostAddress == null || config.hostAddress.trim().isEmpty()) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing host address");
          return;
        }
        if (config.port == 0) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing port number");
          return;
        }

        WMBusMessageListenerAdapter listener = new WMBusMessageListenerAdapter(dispatcher, reconnect);
        WMBusTcpBuilder builder = new WMBusTcpBuilder(
          config.manufacturer, listener, config.hostAddress.trim(), config.port
        );
        int timeout = Optional.of(config.connectionTimeout)
          .filter(val -> val > 0)
          .map(val -> (int) TimeUnit.SECONDS.toMillis(val))
          .orElse(10000);
        builder.setConnectionTimeout(timeout);
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
