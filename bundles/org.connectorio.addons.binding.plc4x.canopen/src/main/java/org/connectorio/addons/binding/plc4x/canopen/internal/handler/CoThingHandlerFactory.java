/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
import org.connectorio.addons.binding.plc4x.canopen.discovery.CoDiscoveryParticipant;
import org.connectorio.plc4x.extras.osgi.PlcDriverManager;
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
public class CoThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final List<CoDiscoveryParticipant> participants = new CopyOnWriteArrayList<>();
  private final PlcDriverManager driverManager;

  @Activate
  public CoThingHandlerFactory(@Reference PlcDriverManager driverManager) {
    super(CANopenBindingConstants.SUPPORTED_THINGS);
    this.driverManager = driverManager;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (CANopenBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE.equals(thing.getThingTypeUID())) {
        return new CoSocketCANBridgeHandler((Bridge) thing, driverManager, participants);
      }
      if (CANopenBindingConstants.NODE_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new CoNodeBridgeHandler((Bridge) thing);
      }
    }

    if (CANopenBindingConstants.RECEIVE_PDO_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new CoPdoReceiveHandler(thing);
    }
    if (CANopenBindingConstants.TRANSMIT_PDO_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new CoPdoTransmitHandler(thing);
    }

    return null;
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void addDiscoveryParticipant(CoDiscoveryParticipant participant) {
    participants.add(participant);
  }

  public void removeDiscoveryParticipant(CoDiscoveryParticipant participant) {
    participants.remove(participant);
  }

}
