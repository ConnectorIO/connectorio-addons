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
package org.connectorio.addons.binding.bacnet.internal;

import org.code_house.bacnet4j.wrapper.api.Type;
import org.connectorio.addons.binding.bacnet.internal.handler.network.BACnetIpv4BridgeHandler;
import org.connectorio.addons.binding.bacnet.internal.handler.network.BACnetMstpBridgeHandler;
import org.connectorio.addons.binding.bacnet.internal.handler.object.BACnetIpDeviceHandler;
import org.connectorio.addons.binding.bacnet.internal.handler.object.BACnetMstpDeviceHandler;
import org.connectorio.addons.binding.bacnet.internal.handler.object.BACnetObjectThingHandler;
import org.connectorio.addons.binding.source.SourceFactory;
import org.connectorio.addons.communication.watchdog.WatchdogManager;
import org.connectorio.addons.link.LinkManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {BACnetThingHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class})
public class BACnetThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory, BACnetBindingConstants {

  private final Logger logger = LoggerFactory.getLogger(BACnetThingHandlerFactory.class);

  private final SerialPortManager serialPortManager;
  private final LinkManager linkManager;
  private final WatchdogManager watchdogManager;
  private final SourceFactory sourceFactory;

  @Activate
  public BACnetThingHandlerFactory(@Reference SerialPortManager serialPortManager, @Reference LinkManager linkManager,
      @Reference(target = "(default=true)") SourceFactory sourceFactory,
      @Reference WatchdogManager watchdogManager) {
    this.serialPortManager = serialPortManager;
    this.linkManager = linkManager;
    this.sourceFactory = sourceFactory;
    this.watchdogManager = watchdogManager;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (thing instanceof Bridge) {
      if (IP_DEVICE_THING_TYPE.equals(thingTypeUID)) {
        return new BACnetIpDeviceHandler((Bridge) thing, linkManager, sourceFactory, watchdogManager);
      } else if (MSTP_DEVICE_THING_TYPE.equals(thingTypeUID)) {
          return new BACnetMstpDeviceHandler((Bridge) thing, linkManager, sourceFactory, watchdogManager);
      } else if (IPV4_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
        return new BACnetIpv4BridgeHandler((Bridge) thing);
//      } else if (IPV6_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
//        return new BACnetIpv4BridgeHandler(bundleContext, (Bridge) thing);
      } else if (MSTP_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
        return new BACnetMstpBridgeHandler((Bridge) thing, serialPortManager);
      }
    }

    if (ANALOG_INPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.ANALOG_INPUT, sourceFactory);
    } else if (ANALOG_OUTPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.ANALOG_OUTPUT, sourceFactory);
    } else if (ANALOG_VALUE_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.ANALOG_VALUE, sourceFactory);
    } else if (BINARY_INPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.BINARY_INPUT, sourceFactory);
    } else if (BINARY_OUTPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.BINARY_OUTPUT, sourceFactory);
    } else if (BINARY_VALUE_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.BINARY_VALUE, sourceFactory);
    } else if (MULTISTATE_INPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.MULTISTATE_INPUT, sourceFactory);
    } else if (MULTISTATE_OUTPUT_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.MULTISTATE_OUTPUT, sourceFactory);
    } else if (MULTISTATE_VALUE_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.MULTISTATE_VALUE, sourceFactory);
    } else if (SCHEDULE_THING_TYPE.equals(thingTypeUID)) {
      return new BACnetObjectThingHandler<>(thing, Type.SCHEDULE, sourceFactory);
    }

    return null;
  }

  @Override
  public boolean supportsThingType(ThingTypeUID thingTypeUID) {
    return BINDING_ID.equals(thingTypeUID.getBindingId());
  }

}
