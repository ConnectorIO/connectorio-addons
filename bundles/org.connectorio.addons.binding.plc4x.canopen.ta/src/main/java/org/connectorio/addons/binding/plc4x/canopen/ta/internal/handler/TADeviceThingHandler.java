/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.plc4x.canopen.api.CoConnection;
import org.connectorio.addons.binding.plc4x.canopen.handler.CoBridgeHandler;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DeviceConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.discovery.DiscoveryThingHandlerService;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TADeviceFactory;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.handler.Plc4xBridgeHandler;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xBridgeHandler;
import org.connectorio.plc4x.decorator.phase.Phase;
import org.connectorio.plc4x.decorator.phase.PhaseDecorator;
import org.connectorio.plc4x.decorator.retry.RetryDecorator;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TADeviceThingHandler extends PollingPlc4xBridgeHandler<PlcConnection, DeviceConfig>
  implements Plc4xBridgeHandler<PlcConnection, DeviceConfig>, Consumer<Boolean>, Runnable {

  private final Logger logger = LoggerFactory.getLogger(TADeviceThingHandler.class);
  private int nodeId;
  private int clientId;

  private CoConnection network;
  private CompletableFuture<TADevice> device = new CompletableFuture<>();
  private TADevice taDevice;

  public TADeviceThingHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  public void initialize() {
    DeviceConfig config = getConfigAs(DeviceConfig.class);
    nodeId = config.nodeId;
    clientId = getBridgeHandler().map(CoBridgeHandler::getNodeId).orElse(-1);
    scheduler.execute(this);
  }

  public void run() {
    getPlcConnection().thenAccept(connection -> {
      logger.debug("Activation of handler for CANopen node {}", nodeId);

      CompletableFuture<CoConnection> network = getBridgeHandler().get().getCoConnection(new PhaseDecorator(), new RetryDecorator(2));

      network.thenCompose((networkConnection) -> {
        this.network = networkConnection;
        Phase phase = Phase.create("Device " + nodeId + " initialization");
        CompletableFuture<TADevice> device = new TADeviceFactory().create(networkConnection.getNode(nodeId), clientId);
        phase.addCallback(new Runnable() {
          @Override
          public void run() {
            if (taDevice != null) {
              logger.info("Completed initialization of node {}, created device {}.", nodeId, taDevice);
            } else {
              logger.warn("Failed to initialize device node {}.", nodeId);
            }
          }
        });
        return device;
      }).whenComplete((device, error) -> {
        if (error != null) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
          logger.warn("Could not initialize TA device {}", nodeId, error);
          return;
        }

        device.addStatusCallback(this);
        this.taDevice = device;
        this.taDevice.login();
        this.device.complete(device);

        Map<String, String> properties = new LinkedHashMap<>(getThing().getProperties());
        device.getName().thenApply(value -> properties.put("Name", value));
        device.getFunction().thenApply(value -> properties.put("Function", value));
        device.getVersion().thenApply(value -> properties.put("Version", value));
        device.getSerial().thenApply(value -> properties.put(Thing.PROPERTY_SERIAL_NUMBER, value));
        device.getProductionDate().thenApply(value -> properties.put("ProductionDate", value));
        device.getBootsector().thenApply(value -> properties.put("Bootsector", value));
        device.getHardwareCover().thenApply(value -> properties.put("HardwareCover", value));
        device.getHardwareMains().thenApply(value -> properties.put("HardwareMains", value));
        editThing().withProperties(properties).build();

        updateStatus(ThingStatus.ONLINE);
        logger.info("Loaded device " + device);
      });
    });
  }

  @SuppressWarnings("unchecked")
  private Optional<CoBridgeHandler<?>> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(CoBridgeHandler.class::isInstance)
      .map(CoBridgeHandler.class::cast);
  }

  @Override
  protected CompletableFuture<PlcConnection> getPlcConnection() {
    return getBridgeHandler().get().getConnection();
  }

  protected Optional<PlcConnection> getBridgeConnection() {
    return getBridgeHandler().map(Plc4xBridgeHandler::getConnection).map(CompletableFuture::join);
  }

  @Override
  public void dispose() {
    if (taDevice != null) {
      taDevice.logout();
      taDevice.removeStatusCallback(this);
      taDevice.close();
    }
    if (!device.isDone()) {
      device.cancel(true);
    }
    if (network != null) {
      network.close();
    }
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(DiscoveryThingHandlerService.class);
  }

  @Override
  protected void updateStatus(ThingStatus status) {
    super.updateStatus(status);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

  @Override
  public void accept(Boolean status) {
    if (status) {
      updateStatus(ThingStatus.ONLINE);
      taDevice.reload();
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Device refused login attempt");
    }
  }

  public CompletableFuture<TADevice> getDevice() {
    return device;
  }

}