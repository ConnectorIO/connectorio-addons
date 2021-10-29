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
package org.connectorio.addons.binding.askoheat.internal.handler;

import javax.ws.rs.client.ClientBuilder;
import org.connectorio.addons.binding.askoheat.internal.AskoheatBindingConstants;
import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ThingHandlerFactory.class)
public class AskoheatHandlerFactory extends BaseThingHandlerFactory {

  private final ClientBuilder clientBuilder;

  @Activate
  public AskoheatHandlerFactory(@Reference ClientBuilder clientBuilder) {
    super(AskoheatBindingConstants.ASKOHEAT_THING_TYPE);
    this.clientBuilder = clientBuilder;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (AskoheatBindingConstants.ASKOHEAT_THING_TYPE.equals(thing.getThingTypeUID()) && thing instanceof Bridge) {
      return new AskoheatBridgeHandler((Bridge) thing, clientBuilder);
    }
    return null;
  }
}
