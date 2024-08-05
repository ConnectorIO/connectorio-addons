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
package org.connectorio.addons.binding.ocpp.internal.handler;

import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.binding.ocpp.OcppBindingConstants;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ThingHandlerFactory.class)
public class OcppThingHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private NetworkAddressService networkAddressService;

  @Activate
  public OcppThingHandlerFactory(@Reference NetworkAddressService networkAddressService) {
    super(OcppBindingConstants.SUPPORTED_THING_TYPES);
    this.networkAddressService = networkAddressService;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    if (thing instanceof Bridge) {
      if (thing.getThingTypeUID().equals(OcppBindingConstants.SERVER_THING_TYPE)) {
        return new ServerBridgeHandler((Bridge) thing, networkAddressService);
      }
      if (thing.getThingTypeUID().equals(OcppBindingConstants.CHARGER_THING_TYPE)) {
        return new ChargerThingHandler((Bridge) thing);
      }
    }

    if (thing.getThingTypeUID().equals(OcppBindingConstants.CONNECTOR_THING_TYPE)) {
      return new ConnectorThingHandler(thing);
    }
    return null;
  }
}