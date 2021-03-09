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
package org.connectorio.addons.binding.plc4x.canopen.internal.handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.plc4x.canopen.internal.CANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.discovery.CANopenDiscoveryParticipant;
import org.connectorio.addons.binding.plc4x.osgi.PlcDriverManager;
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
public class CANopenThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final List<CANopenDiscoveryParticipant> participants = new CopyOnWriteArrayList<>();
  private final PlcDriverManager driverManager;

  @Activate
  public CANopenThingHandlerFactory(@Reference PlcDriverManager driverManager) {
    super(
      CANopenBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE, CANopenBindingConstants.GENERIC_BRIDGE_THING_TYPE, CANopenBindingConstants.SDO_THING_TYPE);
    this.driverManager = driverManager;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (CANopenBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE.equals(thing.getThingTypeUID())) {
        return new CANopenSocketCANBridgeHandler((Bridge) thing, driverManager, participants);
      }
      if (CANopenBindingConstants.GENERIC_BRIDGE_THING_TYPE.equals(thing.getThingTypeUID())) {
        return new CANopenGenericBridgeHandler((Bridge) thing);
      }
    }

    if (CANopenBindingConstants.SDO_THING_TYPE.equals(thing.getThingTypeUID())) {
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
