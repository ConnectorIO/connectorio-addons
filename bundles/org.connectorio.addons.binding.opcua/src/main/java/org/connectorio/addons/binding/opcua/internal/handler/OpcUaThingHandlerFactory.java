/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.opcua.internal.handler;

import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.opcua.OpcUaBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(service = ThingHandlerFactory.class)
public class OpcUaThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  @Activate
  public OpcUaThingHandlerFactory() {
    super(OpcUaBindingConstants.SUPPORTED_THING_TYPES);
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (thing.getThingTypeUID().equals(OpcUaBindingConstants.CLIENT_THING_TYPE)) {
        return new ClientBridgeHandler((Bridge) thing);
      }
    } else {
      if (thing.getThingTypeUID().equals(OpcUaBindingConstants.OBJECT_THING_TYPE)) {
        return new ObjectThingHandler(thing);
      }
    }
    return null;
  }
}