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
package org.connectorio.addons.binding.bacnet.internal.discovery;

import static org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.code_house.bacnet4j.wrapper.api.Property;
import org.connectorio.addons.binding.bacnet.internal.handler.object.BACnetDeviceBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BACnetPropertyDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {

  private final Logger logger = LoggerFactory.getLogger(BACnetPropertyDiscoveryService.class);

  private BACnetDeviceBridgeHandler<?, ?> handler;

  public BACnetPropertyDiscoveryService() throws IllegalArgumentException {
    super(null, 300);
  }

  @Override
  protected void startBackgroundDiscovery() {
    startScan();
  }

  @Override
  protected void startScan() {
    handler.getClient().thenAccept(bacNetClient -> {
      List<Property> properties = bacNetClient.getDeviceProperties(handler.getDevice());
      for (Property property : properties) {
        DiscoveryResult result = toDiscoveryResult(handler, property);
        if (result == null) {
          logger.info("Discovery process found a BACnet property {} which is not yet supported by this binding", property);
          continue;
        }
        thingDiscovered(result);
      }
    });
  }

  private DiscoveryResult toDiscoveryResult(BACnetDeviceBridgeHandler<?, ?> bridge, Property property) {
    DiscoveryResultBuilder builder;
    String id = "" + property.getId();

    ThingUID bridgeUID = bridge.getThing().getUID();
    switch (property.getType()) {
      case ANALOG_INPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(ANALOG_INPUT_THING_TYPE, bridgeUID, id));
        break;
      case ANALOG_OUTPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(ANALOG_OUTPUT_THING_TYPE, bridgeUID, id));
        break;
      case ANALOG_VALUE:
        builder = DiscoveryResultBuilder.create(new ThingUID(ANALOG_VALUE_THING_TYPE, bridgeUID, id));
        break;
      case BINARY_INPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(BINARY_INPUT_THING_TYPE, bridgeUID, id));
        break;
      case BINARY_OUTPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(BINARY_OUTPUT_THING_TYPE, bridgeUID, id));
        break;
      case BINARY_VALUE:
        builder = DiscoveryResultBuilder.create(new ThingUID(BINARY_VALUE_THING_TYPE, bridgeUID, id));
        break;
      case MULTISTATE_INPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(MULTISTATE_INPUT_THING_TYPE, bridgeUID, id));
        break;
      case MULTISTATE_OUTPUT:
        builder = DiscoveryResultBuilder.create(new ThingUID(MULTISTATE_OUTPUT_THING_TYPE, bridgeUID, id));
        break;
      case MULTISTATE_VALUE:
        builder = DiscoveryResultBuilder.create(new ThingUID(MULTISTATE_VALUE_THING_TYPE, bridgeUID, id));
        break;
      case SCHEDULE:
        builder = DiscoveryResultBuilder.create(new ThingUID(SCHEDULE_THING_TYPE, bridgeUID, id));
        break;
      default:
        logger.info("Unsupported object type " + property.getType());
        return null;

    }

    return builder.withLabel(property.getName())
      .withProperty("description", property.getDescription())
      .withBridge(bridgeUID)
      .withProperty("instance", property.getId())
      .withProperty("object", property.getBacNet4jIdentifier().toString())
      .withRepresentationProperty("object")
      .build();
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof BACnetDeviceBridgeHandler) {
      this.handler = (BACnetDeviceBridgeHandler<?, ?>) handler;
    }
  }

  @Override
  public ThingHandler getThingHandler() {
    return this.handler;
  }

  @Override
  public void activate() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
    super.activate(properties);
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

}
