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
import org.connectorio.addons.binding.wmbus.internal.config.OpenHABSerialBridgeConfig;
import org.connectorio.addons.binding.wmbus.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.binding.wmbus.internal.discovery.WMBusDiscoveryService;
import org.connectorio.addons.binding.wmbus.internal.dispatch.DefaultWMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.internal.security.MutableKeyStore;
import org.openhab.core.io.transport.serial.SerialPortManager;
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

public abstract class WMBusSerialBridgeBaseHandler<C extends OpenHABSerialBridgeConfig> extends GenericBridgeHandlerBase<C>
  implements WMBusBridgeHandler<C> {

  private final Logger logger = LoggerFactory.getLogger(WMBusJrxtxSerialBridgeHandler.class);

  private CompletableFuture<WMBusConnection> connection;
  private CompletableFuture<WMBusMessageDispatcher> dispatcher = new CompletableFuture<>();

  protected final SerialPortManager serialPortManager;
  protected final DiscoveryCoordinator discoveryCoordinator;

  public WMBusSerialBridgeBaseHandler(Bridge bridge, SerialPortManager serialPortManager, DiscoveryCoordinator discoveryCoordinator) {
    super(bridge);
    this.serialPortManager = serialPortManager;
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  public void initialize() {
    OpenHABSerialBridgeConfig config = getConfigAs(OpenHABSerialBridgeConfig.class);

    if (config.serialPort == null || config.serialPort.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port must be set");
      return;
    }
    if (config.manufacturer == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Manufacturer must be set");
      return;
    }
    if (config.mode == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Mode must be set");
      return;
    }

    DefaultWMBusMessageDispatcher messageDispatcher = new DefaultWMBusMessageDispatcher();
    connection = initialize(messageDispatcher);
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

  protected abstract CompletableFuture<WMBusConnection> initialize(WMBusMessageDispatcher dispatcher);

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
    return getBridgeConfig().map(cfg -> {
      if (cfg.discoverDevices) {
        return Collections.<Class<? extends ThingHandlerService>>singleton(WMBusDiscoveryService.class);
      }
      return Collections.<Class<? extends ThingHandlerService>>emptyList();
    }).orElse(Collections.emptyList());
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
  @Override
  public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
    if (childHandler instanceof WMBusDeviceThingHandler) {
      WMBusDeviceThingHandler handler = (WMBusDeviceThingHandler) childHandler;
      dispatcher.thenAccept(dsp -> dsp.detach(handler));
    }
  }

  @Override
  public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
    if (childHandler instanceof WMBusDeviceThingHandler) {
      WMBusDeviceThingHandler handler = (WMBusDeviceThingHandler) childHandler;
      dispatcher.thenAccept(dsp -> dsp.attach(handler));
    }
  }

}
