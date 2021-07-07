/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.plc4x.canopen.api.CoConnection;
import org.connectorio.addons.binding.plc4x.canopen.handler.CoBridgeHandler;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DeviceConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.builder.DeviceChannelBuilder;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.builder.StandardDeviceChannelBuilder;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.builder.linking.InputObjectLinkStrategy;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.builder.linking.NameLinkStrategy;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel.ChannelHandler;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel.ChannelHandlerFactory;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel.DefaultChannelHandlerFactory;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TADeviceFactory;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.handler.Plc4xBridgeHandler;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xBridgeHandler;
import org.connectorio.plc4x.decorator.phase.Phase;
import org.connectorio.plc4x.decorator.phase.PhaseDecorator;
import org.connectorio.plc4x.decorator.retry.RetryDecorator;
import org.connectorio.plc4x.decorator.throttle.ThrottleDecorator;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TADeviceThingHandler extends PollingPlc4xBridgeHandler<PlcConnection, DeviceConfig>
  implements Plc4xBridgeHandler<PlcConnection, DeviceConfig>, Consumer<Boolean>, Runnable {

  // no safe caller since initialization might wait much longer than default 5000 ms
  private final static ExecutorService initializer = Executors.newSingleThreadExecutor(new NamedThreadFactory("initializer"));
  private final Logger logger = LoggerFactory.getLogger(TADeviceThingHandler.class);

  private final ChannelHandlerFactory channelHandlerFactory = new DefaultChannelHandlerFactory();
  private DeviceConfig config;

  private int clientId;

  private CoConnection network;
  private CompletableFuture<TADevice> device;
  private Map<ChannelUID, ChannelHandler<?, ?, ?>> channelHandlers = new ConcurrentHashMap<>();
  private TADevice taDevice;
  private DeviceChannelBuilder builder;

  public TADeviceThingHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  public void initialize() {
    this.config = getConfigAs(DeviceConfig.class);
    clientId = getBridgeHandler().map(CoBridgeHandler::getNodeId).orElse(-1);
    device = new CompletableFuture<>();

    initializer.execute(this);
  }

  public void run() {
    getPlcConnection().thenAccept(connection -> {
      logger.debug("Activation of handler for CANopen node {}", config.nodeId);

      final Phase phase = new Phase("Device " + config.nodeId + " initialization", 5_000);
      CompletableFuture<CoConnection> network = getBridgeHandler().get().getCoConnection(new PhaseDecorator(phase));

      network.thenAccept((networkConnection) -> {
        this.network = networkConnection;
        this.taDevice = new TADeviceFactory().get(config.deviceType, networkConnection.getNode(config.nodeId), clientId);
        this.taDevice.login();
        this.taDevice.addStatusCallback(this);

        logger.info("Loaded device " + taDevice);
        phase.onError(new Runnable() {
          @Override
          public void run() {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not retrieve device configuration.");
            taDevice.logout();
          }
        });
        phase.onCompletion(new Runnable() {
          @Override
          public void run() {
            if (builder != null) {
              Map<String, Object> configuration = new HashMap<>(getThing().getConfiguration().getProperties());
              configuration.put("reload", false);
              builder.withConfiguration(new Configuration(configuration));
              builder.build().whenComplete((result, error) -> {
                if (error != null) {
                  logger.warn("Failed to refresh input/output mapping", error);
                  return;
                }
                logger.debug("Updating input/output mapping to {}", result);
                updateThing(result);
              });
              logger.info("Refreshed definition of thing representing {} {}.", config.nodeId, taDevice);
              builder = null;
            }

            if (taDevice != null && device != null) {
              logger.info("Completed initialization of node {}, created device {}.", config.nodeId, taDevice);
              updateStatus(ThingStatus.ONLINE);
              device.complete(taDevice);
            } else {
              logger.warn("Failed to initialize device node {}.", config.nodeId);
              updateStatus(ThingStatus.OFFLINE);
              if (device != null) {
                device.completeExceptionally(new TimeoutException());
              }
            }

            logger.info("Creating individual handlers for configured channels");
            for (Channel channel : getThing().getChannels()) {
              ChannelHandler<?, ?, ?> handler = channelHandlerFactory.create(getCallback(), taDevice, channel);
              handler.initialize();
              channelHandlers.put(channel.getUID(), handler);
            }

            taDevice.setInitialized(true);
            taDevice.logout();
          }
        });
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
    for (ChannelHandler<?, ?, ?> handler : channelHandlers.values()) {
      handler.dispose();
    }

    if (taDevice != null) {
      logger.info("Shutting down handler for device {}", taDevice);
      taDevice.removeStatusCallback(this);
      taDevice.close();
      taDevice = null;
    }

    if (device != null) {
      if (!device.isDone()) {
        device.cancel(true);
      }
      device = null;
    }

    if (network != null) {
      network.close();
      network = null;
    }
  }

//  @Override
//  public Collection<Class<? extends ThingHandlerService>> getServices() {
//    return Collections.singleton(DiscoveryThingHandlerService.class);
//  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (channelHandlers.containsKey(channelUID)) {
      ChannelHandler<?, ?, ?> handler = channelHandlers.get(channelUID);
      if (handler != null) {
        handler.handleCommand(command);
      }
    }
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

  @Override
  public void accept(Boolean status) {
    logger.debug("Login/logout response {} for {}, always reload {}, reload: {}", status, taDevice, config.alwaysReload, config.reload);
    if (getThing().getStatus() == ThingStatus.ONLINE) {
      // if we become online there is no way to get offline.. only bus access errors can put us offline
      return;
    }

    if (status) {
      // status should be amended by phase completion callback
      //updateStatus(ThingStatus.ONLINE);
      if (config.reload()) {
        InputObjectLinkStrategy linkStrategy = config.inputOutputLinkMode == null ? new NameLinkStrategy() : config.inputOutputLinkMode.getStrategy();
        builder = new StandardDeviceChannelBuilder(getThing().getUID(), editThing().withChannels(), config, linkStrategy);

        Map<String, String> properties = new LinkedHashMap<>(getThing().getProperties());
        taDevice.getName().thenApply(value -> properties.put("Name", value));
        taDevice.getFunction().thenApply(value -> properties.put("Function", value));
        taDevice.getVersion().thenApply(value -> properties.put("Version", value));
        taDevice.getSerial().thenApply(value -> properties.put(Thing.PROPERTY_SERIAL_NUMBER, value));
        taDevice.getProductionDate().thenApply(value -> properties.put("ProductionDate", value));
        taDevice.getBootsector().thenApply(value -> properties.put("Bootsector", value));
        taDevice.getHardwareCover().thenApply(value -> properties.put("HardwareCover", value));
        taDevice.getHardwareMains().thenApply(value -> properties.put("HardwareMains", value));

        // an empty .withChannels call force clean up of all existing thing channels
        builder.withProperties(properties).build();
        taDevice.addInOutCallback(builder);
        taDevice.reload();
      } else {
        updateStatus(ThingStatus.ONLINE);
      }
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Device refused login attempt");
    }
  }

  public CompletableFuture<TADevice> getDevice() {
    return device;
  }

  @Override
  protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
    if (status == ThingStatus.ONLINE) {
      if (device != null) {
        // force publishing of units right after we got online so our updates make sense to recipients
        taDevice.sendUnits();
      }
    }

    super.updateStatus(status, statusDetail, description);
  }
}