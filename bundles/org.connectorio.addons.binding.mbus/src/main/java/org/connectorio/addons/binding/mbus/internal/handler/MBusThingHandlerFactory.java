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
package org.connectorio.addons.binding.mbus.internal.handler;

import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.mbus.MBusBindingConstants;
import org.connectorio.addons.binding.mbus.internal.discovery.DiscoveryCoordinator;
import org.connectorio.addons.binding.mbus.internal.handler.converter.Converter;
import org.connectorio.addons.binding.source.SourceFactory;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ThingHandlerFactory.class)
public class MBusThingHandlerFactory extends BaseThingHandlerFactory {

  private final SerialPortManager serialPortManager;
  private final SourceFactory sourceFactory;
  private final Converter converter;
  private final DiscoveryCoordinator discoveryCoordinator;

  @Activate
  public MBusThingHandlerFactory(@Reference SerialPortManager serialPortManager, @Reference(target = "(default=true)") SourceFactory sourceFactory,
    @Reference Converter converter, @Reference DiscoveryCoordinator discoveryCoordinator) {
    super(MBusBindingConstants.SUPPORTED_THING_TYPES);
    this.serialPortManager = serialPortManager;
    this.sourceFactory = sourceFactory;
    this.converter = converter;
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (MBusBindingConstants.SERIAL_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new MBusSerialBridgeHandler((Bridge) thing, serialPortManager, discoveryCoordinator);
      }
      if (MBusBindingConstants.TCP_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new MBusTcpBridgeHandler((Bridge) thing, discoveryCoordinator);
      }
    }

    if (MBusBindingConstants.DEVICE_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new MBusDeviceThingHandler(thing, converter, sourceFactory);
    }

    return null;
  }
}
