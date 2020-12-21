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
package org.connectorio.addons.binding.plc4x;

import java.util.Set;
import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Implementation of base class for PLC4X specific handler factories.
 */
public abstract class Plc4xHandlerFactory extends BaseThingHandlerFactory {

  public Plc4xHandlerFactory(ThingTypeUID ... supportedThings) {
    super(supportedThings);
  }

  public Plc4xHandlerFactory(Set<ThingTypeUID> supportedThings) {
    super(supportedThings);
  }
}
