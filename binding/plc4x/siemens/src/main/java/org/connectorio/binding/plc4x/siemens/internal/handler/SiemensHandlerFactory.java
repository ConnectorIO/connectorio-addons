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
package org.connectorio.binding.plc4x.siemens.internal.handler;

import static org.connectorio.binding.plc4x.siemens.internal.SiemensBindingConstants.THING_TYPE_S7;
import static org.connectorio.binding.plc4x.siemens.internal.SiemensBindingConstants.THING_TYPE_TCP_IP;

import org.connectorio.binding.plc4x.shared.Plc4xHandlerFactory;
import org.connectorio.binding.plc4x.shared.osgi.PlcDriverManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SiemensHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.siemens", service = ThingHandlerFactory.class)
public class SiemensHandlerFactory extends Plc4xHandlerFactory {

  private final PlcDriverManager driverManager;

  @Activate
  public SiemensHandlerFactory(@Reference  PlcDriverManager driverManager) {
    super(THING_TYPE_TCP_IP, THING_TYPE_S7);
    this.driverManager = driverManager;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_TCP_IP.equals(thingTypeUID)) {
      return new SiemensNetworkBridgeHandler((Bridge) thing, driverManager);
    } else if (THING_TYPE_S7.equals(thingTypeUID)) {
      return new SiemensPlcHandler(thing);
    }

    return null;
  }
}
