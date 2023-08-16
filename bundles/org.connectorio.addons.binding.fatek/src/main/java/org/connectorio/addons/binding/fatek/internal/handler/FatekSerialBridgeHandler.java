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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.connectorio.addons.binding.fatek.config.TcpBridgeConfig;
import org.connectorio.addons.binding.fatek.internal.transport.JFatekSerialFaconConnection;
import org.connectorio.addons.binding.fatek.internal.transport.JFatekTcpFaconConnection;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.fatek.config.SerialBridgeConfig;
import org.connectorio.addons.binding.fatek.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.io.transport.serial.config.DataBits;
import org.connectorio.addons.io.transport.serial.config.Parity;
import org.connectorio.addons.io.transport.serial.config.StopBits;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.simplify4u.jfatek.io.FatekConnection;
import org.simplify4u.jfatek.io.FatekConnectionFactory;
import org.simplify4u.jfatek.io.FatekIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FatekSerialBridgeHandler extends GenericBridgeHandlerBase<SerialBridgeConfig> implements
  FatekBridgeHandler<SerialBridgeConfig> {

  private final Logger logger = LoggerFactory.getLogger(FatekSerialBridgeHandler.class);
  private final FatekConnectionFactory serialConnectionFactory;
  private final DiscoveryCoordinator discoveryCoordinator;
  private CompletableFuture<FaconConnection> connection = new CompletableFuture<>();
  private ExecutorService executor;
  private Long refreshInterval;

  public FatekSerialBridgeHandler(Bridge bridge, FatekConnectionFactory serialConnectionFactory, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge);
    this.serialConnectionFactory = serialConnectionFactory;
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  public void initialize() {
    SerialBridgeConfig config = getBridgeConfig().orElse(null);

    if (config == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing configuration");
      return;
    }

    scheduler.execute(() -> {
      if (config.serialPort == null || config.serialPort.trim().isEmpty()) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing serial port");
        return;
      }

      int timeout = config.connectionTimeout != 0 ? config.connectionTimeout : 5000;
      executor = Executors.newFixedThreadPool(2, (runnable) -> new Thread(runnable, "fatek-connection-" + config.serialPort));
      Map<String, Object> params = serialPortParams(config);


      try {
        JFatekSerialFaconConnection faconConnection = new JFatekSerialFaconConnection(executor, config.serialPort, timeout, params);
        this.refreshInterval = config.refreshInterval;
        connection.complete(faconConnection);
        updateStatus(ThingStatus.ONLINE);
      } catch (FatekIOException e) {
        connection.completeExceptionally(e);
      }
    });
  }

  private Map<String, Object> serialPortParams(SerialBridgeConfig config) {
    // defaults from config/facon serial spec
    Map<String, Object> params = new HashMap<>() {{
      put("baudRate", "9600");
      put("dataBits", DataBits.DATABITS_7.name());
      put("stopBits", StopBits.STOPBITS_1.name());
      put("parity", Parity.PARITY_EVEN.name());
    }};
    if (config.baudRate != null) {
      params.put("baudRate", config.baudRate);
    }
    if (config.dataBits != null) {
      params.put("dataBits", config.dataBits.name());
    }
    if (config.stopBits != null) {
      params.put("stopBits", config.stopBits.name());
    }
    if (config.parity != null) {
      params.put("parity", config.parity.name());
    }
    return params;
  }

  @Override
  public void dispose() {
    if (connection.isDone()) {
      try {
        connection.join().close();
      } catch (IOException e) {
        logger.warn("Graceful shutdown of TCP connection failed", e);
      }
    }
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
