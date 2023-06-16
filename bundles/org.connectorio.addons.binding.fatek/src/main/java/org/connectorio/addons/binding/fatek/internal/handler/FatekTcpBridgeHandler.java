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
package org.connectorio.addons.binding.fatek.internal.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.connectorio.addons.binding.fatek.internal.transport.JFatekTcpFaconConnection;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.fatek.config.TcpBridgeConfig;
import org.connectorio.addons.binding.fatek.internal.discovery.DiscoveryCoordinator;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.simplify4u.jfatek.io.FatekIOException;

public class FatekTcpBridgeHandler extends GenericBridgeHandlerBase<TcpBridgeConfig> implements
  FatekBridgeHandler<TcpBridgeConfig> {

  private final DiscoveryCoordinator discoveryCoordinator;

  private ExecutorService executor;
  private CompletableFuture<FaconConnection> connection;

  public FatekTcpBridgeHandler(Bridge thing, DiscoveryCoordinator discoveryCoordinator) {
    super(thing);
    this.discoveryCoordinator = discoveryCoordinator;
  }


  @Override
  public void initialize() {
    TcpBridgeConfig config = getBridgeConfig().orElse(null);

    ThreadPoolManager.getPool("fatek");

    if (config == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing configuration");
      return;
    }

    scheduler.execute(() -> {
      if (config.hostAddress == null || config.hostAddress.trim().isEmpty()) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing host address");
        return;
      }
      if (config.port == 0) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing port number");
        return;
      }
      int timeout = config.connectionTimeout != 0 ? config.connectionTimeout : 5000;
      executor = Executors.newFixedThreadPool(2, (runnable) -> new Thread(runnable, "fatek-connection-" + config.hostAddress));

      try {
        JFatekTcpFaconConnection faconConnection = new JFatekTcpFaconConnection(executor, config.hostAddress, config.port, timeout);
        connection.complete(faconConnection);
        updateStatus(ThingStatus.ONLINE);
      } catch (FatekIOException e) {
        connection.completeExceptionally(e);
      }
    });

  }

  @Override
  public void dispose() {
    if (executor != null) {
      executor.shutdownNow();
    }

    super.dispose();
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public Long getRefreshInterval() {
    return 1000L;
  }

  @Override
  public CompletableFuture<FaconConnection> getConnection() {
    return connection;
  }

  @Override
  public CompletionStage<DiscoveryCoordinator> getDiscoveryCoordinator() {
    return CompletableFuture.completedFuture(discoveryCoordinator);
  }
}
