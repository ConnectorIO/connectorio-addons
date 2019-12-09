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
package org.connectorio.binding.plc4x.siemens.internal.handler;

import static org.connectorio.binding.plc4x.siemens.internal.SiemensBindingConstants.THING_TYPE_S7;
import static org.connectorio.binding.plc4x.siemens.internal.SiemensBindingConstants.THING_TYPE_TCP_IP;

import org.connectorio.binding.plc4x.shared.Plc4xHandlerFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link SiemensHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.siemens", service = ThingHandlerFactory.class)
public class SiemensHandlerFactory extends Plc4xHandlerFactory {

  public SiemensHandlerFactory() {
    super(THING_TYPE_TCP_IP, THING_TYPE_S7);
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_TCP_IP.equals(thingTypeUID)) {
      return new SiemensNetworkBridgeHandler((Bridge) thing);
    } else if (THING_TYPE_S7.equals(thingTypeUID)) {
      return new SiemensPlcHandler(thing);
    }

    return null;
  }
}
