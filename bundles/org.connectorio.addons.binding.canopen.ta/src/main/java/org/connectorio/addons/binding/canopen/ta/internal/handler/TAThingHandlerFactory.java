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
package org.connectorio.addons.binding.canopen.ta.internal.handler;

import static org.connectorio.addons.binding.canopen.ta.internal.TACANopenBindingConstants.*;

import java.util.concurrent.Semaphore;
import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.canopen.ta.internal.TACANopenBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;

@Component(service = {TAThingHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class})
public class TAThingHandlerFactory extends BaseThingHandlerFactory {

  private Semaphore semaphore = new Semaphore(1);

  public TAThingHandlerFactory() {
    super(TACANopenBindingConstants.ALL_SUPPORTED_THINGS);
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (TA_DEVICE_THING_TYPE.equals(thing.getThingTypeUID())) {
        return new TADeviceThingHandler(((Bridge) thing));
      }
    }

    if (TA_UVR_16x2_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new TAUVR16x2ThingHandler(thing, semaphore);
    }

    return null;
  }
}
