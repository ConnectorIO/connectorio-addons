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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal;

import java.util.Collections;
import java.util.Set;
import org.connectorio.addons.binding.plc4x.canopen.CANopenBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

public interface TACANopenBindingConstants extends CANopenBindingConstants {

  // thing types
  String TA_UVR_16x2 = "ta-uvr16x2";

  ThingTypeUID TA_UVR_16x2_THING_TYPE = new ThingTypeUID(BINDING_ID, TA_UVR_16x2);

  Set<ThingTypeUID> SUPPORTED_THINGS = Collections.singleton(TA_UVR_16x2_THING_TYPE);

  String TA_ANALOG_OUTPUT = "ta-analog-output";

  ChannelTypeUID ANALOG_OUTPUT_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_OUTPUT);
  ChannelTypeUID DIGITAL_OUTPUT_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, "ta-digital-output");

}
