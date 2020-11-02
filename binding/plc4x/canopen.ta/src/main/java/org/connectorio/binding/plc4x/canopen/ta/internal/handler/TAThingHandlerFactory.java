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
package org.connectorio.binding.plc4x.canopen.ta.internal.handler;

import static org.connectorio.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants.TA_UVR_16x2_THING_TYPE;

import org.connectorio.binding.base.handler.factory.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;

@Component(service = {TAThingHandlerFactory.class, BaseThingHandlerFactory.class, ThingHandlerFactory.class})
public class TAThingHandlerFactory extends BaseThingHandlerFactory {

  public TAThingHandlerFactory() {
    super(TA_UVR_16x2_THING_TYPE);
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (TA_UVR_16x2_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new TAUVR16x2ThingHandler(thing);
    }
    return null;
  }
}
