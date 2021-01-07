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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants.TA_UVR_16x2_THING_TYPE;

import java.util.concurrent.Semaphore;
import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;

@Component(service = {TAThingHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class})
public class TAThingHandlerFactory extends BaseThingHandlerFactory {

  private Semaphore semaphore = new Semaphore(1);

  public TAThingHandlerFactory() {
    super(TA_UVR_16x2_THING_TYPE);
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (TA_UVR_16x2_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new TAUVR16x2ThingHandler(thing, semaphore);
    }
    return null;
  }
}
