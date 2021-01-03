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
package org.connectorio.addons.binding.relayweblog.internal.handler;

import javax.ws.rs.client.ClientBuilder;
import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ThingHandlerFactory.class)
public class RelayWeblogThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final ClientBuilder clientBuilder;

  @Activate
  public RelayWeblogThingHandlerFactory(@Reference ClientBuilder clientBuilder) {
    super(RelayWeblogBindingConstants.WEBLOG_THING_TYPE, RelayWeblogBindingConstants.METER_THING_TYPE);
    this.clientBuilder = clientBuilder;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (RelayWeblogBindingConstants.WEBLOG_THING_TYPE.equals(thing.getThingTypeUID())) {
        return new WeblogBridgeHandler((Bridge) thing, clientBuilder);
      }
    }

    if (RelayWeblogBindingConstants.METER_THING_TYPE.equals(thing.getThingTypeUID())) {
      return new MeterThingHandler(thing);
    }

    return null;
  }

}
