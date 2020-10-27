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
package org.connectorio.binding.can.internal;

//import static org.connectorio.binding.can.internal.CANBindingConstants.*;
//
//import org.connectorio.binding.can.internal.handler.SocketCANBridgeHandler;
//import org.openhab.core.thing.Bridge;
//import org.openhab.core.thing.Thing;
//import org.openhab.core.thing.ThingTypeUID;
//import org.openhab.core.thing.binding.BaseThingHandlerFactory;
//import org.openhab.core.thing.binding.ThingHandler;
//import org.openhab.core.thing.binding.ThingHandlerFactory;
//import org.osgi.service.component.annotations.Component;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Component(service = {CANThingHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class})
//public class CANThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {
//
//  private final Logger logger = LoggerFactory.getLogger(CANThingHandlerFactory.class);
//
//  @Override
//  protected ThingHandler createHandler(Thing thing) {
//    ThingTypeUID thingTypeUID = thing.getThingTypeUID();
//
//    if (thing instanceof Bridge) {
//      if (SOCKETCAN_BRIDGE_THING_TYPE.equals(thingTypeUID)) {
//        return new SocketCANBridgeHandler((Bridge) thing);
//      }
//    }
//
//    return null;
//  }
//
//  @Override
//  public boolean supportsThingType(ThingTypeUID thingTypeUID) {
//    return BINDING_ID.equals(thingTypeUID.getBindingId());
//  }
//
//}
