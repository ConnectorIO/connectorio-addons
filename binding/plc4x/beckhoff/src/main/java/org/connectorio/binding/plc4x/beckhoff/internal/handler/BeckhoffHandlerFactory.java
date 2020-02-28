/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional information.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_ADS;
import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_NETWORK;
import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_SERIAL;

import org.connectorio.binding.plc4x.shared.Plc4xHandlerFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BeckhoffHandlerFactory} is responsible for creating handlers fod ads enabled elements.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.beckhoff", service = ThingHandlerFactory.class)
public class BeckhoffHandlerFactory extends Plc4xHandlerFactory {

  public BeckhoffHandlerFactory() {
    super(THING_TYPE_NETWORK, THING_TYPE_SERIAL, THING_TYPE_ADS);
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_NETWORK.equals(thingTypeUID)) {
      return new BeckhoffNetworkBridgeHandler((Bridge) thing);
    } else if (THING_TYPE_SERIAL.equals(thingTypeUID)) {
      return new BeckhoffSerialBridgeHandler((Bridge) thing);
    } else if (THING_TYPE_ADS.equals(thingTypeUID)) {
      return new BeckhoffPlcHandler(thing);
    }

    return null;
  }
}
