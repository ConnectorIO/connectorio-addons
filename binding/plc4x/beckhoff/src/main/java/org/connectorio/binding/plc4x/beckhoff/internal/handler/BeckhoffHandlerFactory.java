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
package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_ADS;
import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_AMS;
import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_NETWORK;
import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_SERIAL;

import org.connectorio.binding.plc4x.beckhoff.internal.discovery.DiscoveryReceiver;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.DiscoverySender;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.RouteReceiver;
import org.connectorio.binding.plc4x.shared.Plc4xHandlerFactory;
import org.connectorio.binding.plc4x.shared.osgi.PlcDriverManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BeckhoffHandlerFactory} is responsible for creating handlers fod ads enabled elements.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.beckhoff", service = ThingHandlerFactory.class)
public class BeckhoffHandlerFactory extends Plc4xHandlerFactory {

  private final PlcDriverManager driverManager;
  private final DiscoverySender sender;
  private final DiscoveryReceiver receiver;
  private final RouteReceiver router;

  @Activate
  public BeckhoffHandlerFactory(@Reference PlcDriverManager driverManager, @Reference DiscoverySender sender, @Reference DiscoveryReceiver receiver, @Reference RouteReceiver router) {
    super(THING_TYPE_AMS, THING_TYPE_NETWORK, THING_TYPE_SERIAL, THING_TYPE_ADS);
    this.driverManager = driverManager;
    this.sender = sender;
    this.receiver = receiver;
    this.router = router;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_AMS.equals(thingTypeUID)) {
      return new BeckhoffAmsAdsBridgeHandler((Bridge) thing, sender, receiver);
    } else if (THING_TYPE_NETWORK.equals(thingTypeUID)) {
      return new BeckhoffNetworkBridgeHandler((Bridge) thing, driverManager, sender, router);
    } else if (THING_TYPE_SERIAL.equals(thingTypeUID)) {
      return new BeckhoffSerialBridgeHandler((Bridge) thing, driverManager);
    } else if (THING_TYPE_ADS.equals(thingTypeUID)) {
      return new BeckhoffPlcHandler(thing);
    }

    return null;
  }
}
