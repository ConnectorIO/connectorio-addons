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
package org.connectorio.addons.binding.wmbus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.connectorio.addons.binding.BaseBindingConstants;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

public interface WMBusBindingConstants extends BaseBindingConstants {

  String BINDING_ID = BaseBindingConstants.identifier("wmbus");

  ThingTypeUID DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "device");

  ThingTypeUID TCP_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "tcp");
  ThingTypeUID SERIAL_BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "serial");

  Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(
    DEVICE_THING_TYPE,
    TCP_BRIDGE_TYPE,
    SERIAL_BRIDGE_TYPE
  ));

  // static channels
  ChannelTypeUID CHANNEL_TYPE_RSSI = new ChannelTypeUID(BINDING_ID, "rssi");

  // common dynamic channel types
  ChannelTypeUID CHANNEL_TYPE_NUMBER = new ChannelTypeUID(BINDING_ID, "number");
  ChannelTypeUID CHANNEL_TYPE_DATETIME = new ChannelTypeUID(BINDING_ID, "datetime");
  ChannelTypeUID CHANNEL_TYPE_STRING = new ChannelTypeUID(BINDING_ID, "string");

  // known thing properties
  String THING_PROPERTY_ENCRYPTED = "encrypted";
}
