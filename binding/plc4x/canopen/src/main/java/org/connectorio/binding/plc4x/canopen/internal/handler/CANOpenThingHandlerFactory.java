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
package org.connectorio.binding.plc4x.canopen.internal.handler;

import static org.connectorio.binding.plc4x.canopen.internal.CANopenBindingConstants.GENERIC_BRIDGE_THING_TYPE;
import static org.connectorio.binding.plc4x.canopen.internal.CANopenBindingConstants.SDO_THING_TYPE;
import static org.connectorio.binding.plc4x.canopen.internal.CANopenBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.connectorio.binding.base.handler.factory.BaseThingHandlerFactory;
import org.connectorio.binding.plc4x.canopen.discovery.CANopenDiscoveryParticipant;
import org.connectorio.binding.plc4x.shared.osgi.PlcDriverManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(service = ThingHandlerFactory.class)
public class CANOpenThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final List<CANopenDiscoveryParticipant> participants = new CopyOnWriteArrayList<>();
  private final PlcDriverManager driverManager;

  @Activate
  public CANOpenThingHandlerFactory(@Reference PlcDriverManager driverManager) {
    super(SOCKETCAN_BRIDGE_THING_TYPE, GENERIC_BRIDGE_THING_TYPE, SDO_THING_TYPE);
    this.driverManager = driverManager;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (SOCKETCAN_BRIDGE_THING_TYPE.equals(thing.getThingTypeUID())) {
        return new CANOpenSocketCANBridgeHandler((Bridge) thing, driverManager, participants);
      }
      if (GENERIC_BRIDGE_THING_TYPE.equals(thing.getThingTypeUID())) {
        return new CANOpenGenericBridgeHandler((Bridge) thing);
      }
    }

    if (SDO_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new SDOThingHandler(thing);
    }

    return null;
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void addDiscoveryParticipant(CANopenDiscoveryParticipant participant) {
    participants.add(participant);
  }

  public void removeDiscoveryParticipant(CANopenDiscoveryParticipant participant) {
    participants.remove(participant);
  }

}
