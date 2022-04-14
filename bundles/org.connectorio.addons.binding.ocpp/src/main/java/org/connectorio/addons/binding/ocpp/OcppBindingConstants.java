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
package org.connectorio.addons.binding.ocpp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.connectorio.addons.binding.BaseBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

public interface OcppBindingConstants extends BaseBindingConstants {

  String BINDING_ID = BaseBindingConstants.identifier("ocpp");

  ThingTypeUID SERVER_THING_TYPE = new ThingTypeUID(BINDING_ID, "server");
  ThingTypeUID CHARGER_THING_TYPE = new ThingTypeUID(BINDING_ID, "charger");
  ThingTypeUID CONNECTOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "connector");

  Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(
    SERVER_THING_TYPE,
    CHARGER_THING_TYPE,
    CONNECTOR_THING_TYPE
  ));

}
