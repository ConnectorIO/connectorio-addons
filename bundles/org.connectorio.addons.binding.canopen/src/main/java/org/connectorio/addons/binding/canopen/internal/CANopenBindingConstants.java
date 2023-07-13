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
package org.connectorio.addons.binding.canopen.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openhab.core.thing.ThingTypeUID;

public interface CANopenBindingConstants extends org.connectorio.addons.binding.canopen.CANopenBindingConstants {

  // bridge types
  String SOCKETCAN_BRIDGE_TYPE = "socketcan";

  String NODE_THING = "node";
  String RECEIVE_PDO_THING = "rpdo";
  String TRANSMIT_PDO_THING = "tpdo";

  ThingTypeUID SOCKETCAN_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, SOCKETCAN_BRIDGE_TYPE);
  ThingTypeUID NODE_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, NODE_THING);
  ThingTypeUID RECEIVE_PDO_THING_TYPE = new ThingTypeUID(BINDING_ID, RECEIVE_PDO_THING);
  ThingTypeUID TRANSMIT_PDO_THING_TYPE = new ThingTypeUID(BINDING_ID, TRANSMIT_PDO_THING);

  Set<ThingTypeUID> SUPPORTED_THINGS = new HashSet<>(Arrays.asList(SOCKETCAN_BRIDGE_THING_TYPE,
    NODE_BRIDGE_TYPE, RECEIVE_PDO_THING_TYPE, TRANSMIT_PDO_THING_TYPE
  ));
}
