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
package org.connectorio.addons.binding.smartme.internal.handler;

import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.smartme.SmartMeBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

@Component(service = ThingHandlerFactory.class)
public class SmartMeThingHandlerFactory extends BaseThingHandlerFactory {

  public SmartMeThingHandlerFactory() {
    super(SmartMeBindingConstants.SUPPORTED_THINGS);
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (SmartMeBindingConstants.SMARTME_CLOUD_BRIDGE_TYPE.equals(thing.getThingTypeUID())) {
        return new SmartMeCloudThingHandler((Bridge) thing);
      }
    }
    if (SmartMeBindingConstants.SMARTME_DEVICE_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new SmartMeDeviceThingHandler(thing);
    }
    return null;
  }
}
