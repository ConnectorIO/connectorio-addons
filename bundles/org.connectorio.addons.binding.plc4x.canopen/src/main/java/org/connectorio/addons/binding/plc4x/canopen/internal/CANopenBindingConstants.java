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
package org.connectorio.addons.binding.plc4x.canopen.internal;

import org.openhab.core.thing.ThingTypeUID;

public interface CANopenBindingConstants extends org.connectorio.addons.binding.plc4x.canopen.CANopenBindingConstants {

  // bridge types
  String SOCKETCAN_BRIDGE_TYPE = "socketcan";

  String GENERIC_THING = "generic";
  String SDO_THING = "sdo";

  ThingTypeUID SOCKETCAN_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, SOCKETCAN_BRIDGE_TYPE);

  ThingTypeUID GENERIC_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, GENERIC_THING);

  ThingTypeUID SDO_THING_TYPE = new ThingTypeUID(BINDING_ID, SDO_THING);

}
