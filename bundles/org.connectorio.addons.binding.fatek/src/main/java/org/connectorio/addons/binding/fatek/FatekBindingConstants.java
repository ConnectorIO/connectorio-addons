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
  ChannelTypeUID CHANNEL_TYPE_CONTACT = new ChannelTypeUID(BINDING_ID, "contact");
  ChannelTypeUID CHANNEL_TYPE_DISCRETE = new ChannelTypeUID(BINDING_ID, "discrete");
  ChannelTypeUID CHANNEL_TYPE_DATA16 = new ChannelTypeUID(BINDING_ID, "data16");
  ChannelTypeUID CHANNEL_TYPE_DATA32 = new ChannelTypeUID(BINDING_ID, "data32");
  ChannelTypeUID CHANNEL_TYPE_PERCENT16 = new ChannelTypeUID(BINDING_ID, "percent16");
  ChannelTypeUID CHANNEL_TYPE_PERCENT32 = new ChannelTypeUID(BINDING_ID, "percent32");
  ChannelTypeUID CHANNEL_TYPE_ROLLERSHUTTER16 = new ChannelTypeUID(BINDING_ID, "rollershutter16");
  ChannelTypeUID CHANNEL_TYPE_ROLLERSHUTTER32 = new ChannelTypeUID(BINDING_ID, "rollershutter32");
  ChannelTypeUID CHANNEL_TYPE_COLOR16 = new ChannelTypeUID(BINDING_ID, "color16");
  ChannelTypeUID CHANNEL_TYPE_COLOR32 = new ChannelTypeUID(BINDING_ID, "color32");


}
