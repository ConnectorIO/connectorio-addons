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

import org.connectorio.addons.binding.fatek.FatekBindingConstants;
import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.fatek.internal.discovery.DiscoveryCoordinator;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ThingHandlerFactory.class)
public class FatekThingHandlerFactory extends BaseThingHandlerFactory {

  private final SerialPortManager serialPortManager;
  private final DiscoveryCoordinator discoveryCoordinator;

  @Activate
  public FatekThingHandlerFactory(@Reference SerialPortManager serialPortManager, @Reference DiscoveryCoordinator discoveryCoordinator) {
    super(FatekBindingConstants.SUPPORTED_THING_TYPES);
    this.serialPortManager = serialPortManager;
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (FatekBindingConstants.SERIAL_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new FatekSerialBridgeHandler((Bridge) thing, serialPortManager, discoveryCoordinator);
      }
      if (FatekBindingConstants.TCP_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new FatekTcpBridgeHandler((Bridge) thing, discoveryCoordinator);
      }
    }

    if (FatekBindingConstants.PLC_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new FatekPlcThingHandler(thing);
    }

    return null;
  }
}
