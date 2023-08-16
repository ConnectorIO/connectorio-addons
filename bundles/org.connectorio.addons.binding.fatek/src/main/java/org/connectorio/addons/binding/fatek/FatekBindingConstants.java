/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.fatek;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.connectorio.addons.binding.BaseBindingConstants;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

public interface FatekBindingConstants extends BaseBindingConstants {

  String BINDING_ID = BaseBindingConstants.identifier("fatek");

  ThingTypeUID PLC_THING_TYPE = new ThingTypeUID(BINDING_ID, "plc");

  ThingTypeUID SERIAL_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "serial");
  ThingTypeUID TCP_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "tcp");

  Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(
    PLC_THING_TYPE,
    SERIAL_BRIDGE_TYPE,
    TCP_BRIDGE_TYPE
  ));

  // common dynamic channel types
  ChannelTypeUID CHANNEL_TYPE_DISCRETE_INPUT = new ChannelTypeUID(BINDING_ID, "discrete-input");
  ChannelTypeUID CHANNEL_TYPE_DISCRETE_OUTPUT = new ChannelTypeUID(BINDING_ID, "discrete-output");


}
