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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.internal.KeyStore;
import org.connectorio.addons.binding.wmbus.internal.config.BridgeConfig;
import org.connectorio.addons.binding.wmbus.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.binding.wmbus.internal.discovery.WMBusDiscoveryService;
import org.connectorio.addons.binding.wmbus.internal.dispatch.DefaultWMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.internal.security.MutableKeyStore;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WMBusBridgeBaseHandler<C extends BridgeConfig> extends GenericBridgeHandlerBase<C>
  implements WMBusBridgeHandler<C> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private CompletableFuture<WMBusConnection> connection;
  private CompletableFuture<WMBusMessageDispatcher> dispatcher = new CompletableFuture<>();

  protected final DiscoveryCoordinator discoveryCoordinator;

  public WMBusBridgeBaseHandler(Bridge bridge, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge);
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  public void initialize() {
    DefaultWMBusMessageDispatcher messageDispatcher = new DefaultWMBusMessageDispatcher();
    connection = initializeConnection(messageDispatcher);
    connection.whenComplete((connection, error) -> {
      if (error != null) {
        logger.error("Could not open serial connection", error);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not open port");
        return;
      }
      updateStatus(ThingStatus.ONLINE);
      dispatcher.complete(messageDispatcher);
    });
  }

  protected abstract CompletableFuture<WMBusConnection> initializeConnection(WMBusMessageDispatcher dispatcher);

  @Override
  public void dispose() {
    if (connection.isDone()) {
      try {
        connection.get().close();
      } catch (Exception e) {
        logger.error("Failed to close serial connection", e);
      }
    }
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return getBridgeConfig().filter(cfg -> cfg.discoverDevices)
      .map(cfg -> Collections.<Class<? extends ThingHandlerService>>singleton(WMBusDiscoveryService.class))
      .orElse(Collections.emptySet());
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public CompletableFuture<WMBusMessageDispatcher> getDispatcher() {
    return dispatcher;
  }

  public CompletableFuture<KeyStore> getKeyStore() {
    return connection.thenApply(MutableKeyStore::new);
  }

  public CompletableFuture<DiscoveryCoordinator> getDiscoveryCoordinator() {
    return connection.thenApply(connection -> this.discoveryCoordinator);
  }

}
