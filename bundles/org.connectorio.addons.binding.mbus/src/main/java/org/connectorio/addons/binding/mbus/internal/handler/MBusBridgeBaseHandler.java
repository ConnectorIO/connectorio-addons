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
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.mbus.config.BridgeConfig;
import org.connectorio.addons.binding.mbus.internal.discovery.DiscoveryCoordinator;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openmuc.jmbus.MBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MBusBridgeBaseHandler<C extends BridgeConfig> extends GenericBridgeHandlerBase<C>
  implements MBusBridgeHandler<C> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private CompletableFuture<MBusConnection> connection;

  protected final DiscoveryCoordinator discoveryCoordinator;

  public MBusBridgeBaseHandler(Bridge bridge, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge);
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  public void initialize() {
    connection = initializeConnection();
    connection.whenComplete((connection, error) -> {
      if (error != null) {
        logger.error("Could not open serial connection", error);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not open port");
        return;
      }
      updateStatus(ThingStatus.ONLINE);
    });
  }

  protected abstract CompletableFuture<MBusConnection> initializeConnection();

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public CompletableFuture<MBusConnection> getConnection() {
    return connection;
  }

  public CompletableFuture<DiscoveryCoordinator> getDiscoveryCoordinator() {
    return connection.thenApply(connection -> this.discoveryCoordinator);
  }

}