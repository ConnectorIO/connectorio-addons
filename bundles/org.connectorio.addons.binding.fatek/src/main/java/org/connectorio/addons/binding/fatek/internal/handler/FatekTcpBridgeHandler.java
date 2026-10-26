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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.connectorio.addons.binding.fatek.internal.transport.JFatekTcpFaconConnection;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.fatek.config.TcpBridgeConfig;
import org.connectorio.addons.binding.fatek.internal.discovery.DiscoveryCoordinator;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.simplify4u.jfatek.io.FatekIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FatekTcpBridgeHandler extends GenericBridgeHandlerBase<TcpBridgeConfig> implements
  FatekBridgeHandler<TcpBridgeConfig> {

  private final Logger logger = LoggerFactory.getLogger(FatekPlcThingHandler.class);

  private final DiscoveryCoordinator discoveryCoordinator;

  private CompletableFuture<FaconConnection> connection = new CompletableFuture<>();
  private ExecutorService executor;
  private Long refreshInterval;

  public FatekTcpBridgeHandler(Bridge thing, DiscoveryCoordinator discoveryCoordinator) {
    super(thing);
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  public void initialize() {
    TcpBridgeConfig config = getBridgeConfig().orElse(null);

    if (config == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing configuration");
      return;
    }
    executor = Executors.newFixedThreadPool(2, (runnable) -> new Thread(runnable, "fatek-connection-" + getThing().getUID().getId()));

    connect();
  }

  private void connect() {
    TcpBridgeConfig config = getBridgeConfig().orElse(null);

    if (config == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing configuration");
      return;
    }
    if (config.host == null || config.host.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing host address");
      return;
    }
    if (config.port == 0) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing port number");
      return;
    }
    try {
      int timeout = config.connectionTimeout != 0 ? config.connectionTimeout : 5000;
      JFatekTcpFaconConnection faconConnection = new JFatekTcpFaconConnection(executor, config.host, config.port, timeout);
      this.refreshInterval = config.refreshInterval;
      connection.complete(faconConnection);
      updateStatus(ThingStatus.ONLINE);
    } catch (FatekIOException e) {
      connection.completeExceptionally(e);
    }
  }

  private void disconnect() {
    if (connection != null && connection.isDone()) {
      try {
        connection.join().close();
      } catch (IOException e) {
        logger.warn("Error while shutting down connection");
      } finally {
        connection = null;
      }
    }
  }

  @Override
  public Consumer<Exception> getConnectionErrorHandler() {
    return cause -> {
      logger.warn("Fatek TCP connection error, scheduling reconnect in 5s", cause);
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause.getMessage());
      disconnect();
      this.connection = new CompletableFuture<>();
      scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
    };
  }

  @Override
  public void dispose() {
    disconnect();

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
    return refreshInterval;
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
