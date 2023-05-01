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

import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.wmbus.WMBusBindingConstants;
import org.connectorio.addons.binding.wmbus.internal.discovery.DiscoveryCoordinator;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ThingHandlerFactory.class)
public class WMBusThingHandlerFactory extends BaseThingHandlerFactory {

  private final SerialPortManager serialPortManager;
  private final DiscoveryCoordinator discoveryCoordinator;

  @Activate
  public WMBusThingHandlerFactory(@Reference SerialPortManager serialPortManager, @Reference DiscoveryCoordinator discoveryCoordinator) {
    super(WMBusBindingConstants.SUPPORTED_THING_TYPES);
    this.serialPortManager = serialPortManager;
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (WMBusBindingConstants.TCP_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new WMBusTcpBridgeHandler((Bridge) thing, discoveryCoordinator);
      }
      if (WMBusBindingConstants.SERIAL_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new WMBusSerialBridgeHandler((Bridge) thing, serialPortManager, discoveryCoordinator);
      }
    }

    if (WMBusBindingConstants.DEVICE_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new WMBusDeviceThingHandler<>(thing);
    }

    return null;
  }
}
