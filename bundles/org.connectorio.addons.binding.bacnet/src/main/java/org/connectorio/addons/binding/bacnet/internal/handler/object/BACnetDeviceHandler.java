/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.handler.object;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.DeviceObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.addons.binding.bacnet.internal.config.DeviceChannelConfig;
import org.connectorio.addons.binding.bacnet.internal.discovery.BACnetPropertyDiscoveryService;
import org.connectorio.addons.binding.bacnet.internal.config.DeviceConfig;
import org.connectorio.addons.binding.bacnet.internal.handler.BACnetObjectBridgeHandler;
import org.connectorio.addons.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.connectorio.addons.binding.bacnet.internal.handler.object.task.RefreshDeviceTask;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BACnetDeviceHandler<C extends DeviceConfig> extends BACnetObjectBridgeHandler<DeviceObject, BACnetNetworkBridgeHandler<?>, C>
  implements BACnetDeviceBridgeHandler<BACnetNetworkBridgeHandler<?>, C> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Device device;
  private CompletableFuture<BacNetClient> clientFuture = new CompletableFuture<>();
  private Map<Long, ScheduledFuture<?>> pollers = new LinkedHashMap<>();

  /**
   * Creates a new instance of this class for the {@link Thing}.
   *
   * @param bridge the thing that should be handled, not null
   */
  public BACnetDeviceHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<BACnetNetworkBridgeHandler<?>> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(BACnetNetworkBridgeHandler.class::isInstance)
      .map(BACnetNetworkBridgeHandler.class::cast);
  }

  @Override
  public void initialize() {
    device = getBridgeConfig()
      .map(cfg -> {
        Integer networkNumber =  Optional.ofNullable(cfg.network)
          .orElseGet(() -> getBridgeHandler().flatMap(BACnetNetworkBridgeHandler::getNetworkNumber).orElse(0));
        return createDevice(cfg, networkNumber);
      }).orElse(null);

    if (device != null) {
      getBridgeHandler().get().getClient().whenComplete((client, error) -> {
        if (error != null) {
          logger.warn("Initialization of BACnet device handler failed, could not establish client connection", error);
          return;
        }

        if (thing.getChannels().isEmpty()) {
          updateChannels(client);
        }

        Map<Long, Map<ChannelUID, BacNetObject>> pollingMap = new LinkedHashMap<>();
        for (Channel channel : thing.getChannels()) {
          DeviceChannelConfig deviceChannelConfig = channel.getConfiguration().as(DeviceChannelConfig.class);
          Long refreshInterval = Optional.ofNullable(deviceChannelConfig.refreshInterval)
              .filter(value -> value != 0)
              .orElse(getRefreshInterval());
          if (!pollingMap.containsKey(refreshInterval)) {
            pollingMap.put(refreshInterval, new LinkedHashMap<>());
          }
          BacNetObject object = new BacNetObject(device, deviceChannelConfig.instance, deviceChannelConfig.type);
          pollingMap.get(refreshInterval).put(channel.getUID(), object);
        }

        for (Entry<Long, Map<ChannelUID, BacNetObject>> entry : pollingMap.entrySet()) {
          ScheduledFuture<?> poller = scheduler.scheduleAtFixedRate(new RefreshDeviceTask(() -> clientFuture, getCallback(), device, entry.getValue()),
            0, entry.getKey(), TimeUnit.MILLISECONDS);
          pollers.put(entry.getKey(), poller);
        }
        updateStatus(ThingStatus.ONLINE);
        clientFuture.complete(client);
      });
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing device configuration");
    }
  }

  @Override
  public void dispose() {
    if (pollers != null) {
      for (Entry<Long, ScheduledFuture<?>> entry : pollers.entrySet()) {
        try {
          ScheduledFuture<?> value = entry.getValue();
          value.cancel(false);
        } catch (Exception e) {
          logger.warn("Error during shutdown of poller checking device {} every {}ms", device, entry.getKey());
        }
      }
      pollers = null;
    }
    super.dispose();
  }

  private void updateChannels(BacNetClient client) {
    BridgeBuilder builder = editThing();
    builder.withChannels(new ArrayList<>()); // reset channel list
    for (BacNetObject object : client.getDeviceObjects(device)) {
      String channelId = object.getType().name().toLowerCase() + "-" + object.getId() + "-presentValue";
      ChannelUID uid = new ChannelUID(thing.getUID(), channelId);
      String itemType = mapItemType(object);
      ChannelTypeUID channelType = mapChannelType(object);
      if (itemType == null || channelType == null) {
        continue;
      }

      Channel channel = ChannelBuilder.create(uid)
          .withType(channelType)
          .withConfiguration(channelConfiguration(object))
          .withLabel(object.getName())
          .withDescription(object.getDescription())
          .withAcceptedItemType(itemType)
          .build();
      builder.withChannel(channel);
    }
    updateThing(builder.build());
  }

  private String mapItemType(BacNetObject object) {
    switch (object.getType()) {
      case ANALOG_INPUT:
      case ANALOG_OUTPUT:
      case ANALOG_VALUE:
        return CoreItemFactory.NUMBER;
      case BINARY_INPUT:
        return CoreItemFactory.CONTACT;
      case BINARY_OUTPUT:
      case BINARY_VALUE:
        return CoreItemFactory.SWITCH;
      case MULTISTATE_INPUT:
      case MULTISTATE_OUTPUT:
      case MULTISTATE_VALUE:
        return CoreItemFactory.NUMBER;
      case CHARACTER_STRING:
      case OCTET_STRING:
        return CoreItemFactory.STRING;
      case LARGE_ANALOG:
        return CoreItemFactory.NUMBER;
      case DATE_TIME:
      case TIME:
      case DATE_VALUE:
        return CoreItemFactory.DATETIME;
      case INTEGER:
        return CoreItemFactory.NUMBER;
      case POSITIVE_INTEGER:
        return CoreItemFactory.NUMBER;
      case DATE_TIME_PATTERN:
      case DATE_PATTERN:
      case TIME_PATTERN:
        return CoreItemFactory.STRING;
    }
    return null;
  }

  private Configuration channelConfiguration(BacNetObject object) {
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("instance", object.getId());
    properties.put("type", object.getType().name());
    properties.put("readOnly", false);
    return new Configuration(properties);
  }

  private ChannelTypeUID mapChannelType(BacNetObject object) {
    String type = "";
    switch (object.getType()) {
      case ANALOG_INPUT:
      case ANALOG_OUTPUT:
      case ANALOG_VALUE:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "deviceWriteableNumber");
      case BINARY_INPUT:
      case BINARY_OUTPUT:
      case BINARY_VALUE:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "device-WriteableBinary");
      case MULTISTATE_INPUT:
      case MULTISTATE_OUTPUT:
      case MULTISTATE_VALUE:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "deviceWriteableNumber");
      case CHARACTER_STRING:
      case OCTET_STRING:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "deviceWriteableText");
      case LARGE_ANALOG:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "deviceWriteableNumber");
      case DATE_TIME:
      case TIME:
      case DATE_VALUE:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "deviceWriteableDateTime");
      case INTEGER:
      case POSITIVE_INTEGER:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "deviceWrriteableNumber");
      case DATE_TIME_PATTERN:
      case DATE_PATTERN:
      case TIME_PATTERN:
        return new ChannelTypeUID(BACnetBindingConstants.BINDING_ID, "deviceWrriteableText");
    }
    return null;
  }

  protected abstract Device createDevice(C config, Integer networkNumber);

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(BACnetPropertyDiscoveryService.class);
  }

  @Override
  public CompletableFuture<BacNetClient> getClient() {
    return clientFuture;
  }

  @Override
  public Device getDevice() {
    return device;
  }

}
