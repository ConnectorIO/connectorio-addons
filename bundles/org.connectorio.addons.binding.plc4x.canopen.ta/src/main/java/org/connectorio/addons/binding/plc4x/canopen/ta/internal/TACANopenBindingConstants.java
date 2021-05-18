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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.connectorio.addons.binding.plc4x.canopen.CANopenBindingConstants;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

public interface TACANopenBindingConstants extends CANopenBindingConstants {

  // thing types
  String TA_UVR_16x2 = "ta-uvr16x2";

  String TA_DEVICE = "ta-device";
  String TA_DIGITAL = "ta-digital";
  String TA_FUNCTION = "ta-function";

  //String TA_CONFIG_URI = CANopenBindingConstants.BINDING_ID + "-ta";

  String TA_ANALOG_PREFIX = "ta-analog";
  String TA_DIGITAL_PREFIX = "ta-digital";

  String TA_ANALOG_RAS_MODE = TA_ANALOG_PREFIX + "-ras-mode";
  String TA_ANALOG_RAS_TEMPERATURE = TA_ANALOG_PREFIX + "-ras-temperature";
  String TA_ANALOG_TEMPERATURE = TA_ANALOG_PREFIX + "-temperature";
  String TA_ANALOG_LENGTH = TA_ANALOG_PREFIX + "-length";
  String TA_ANALOG_VOLUME = TA_ANALOG_PREFIX + "-volume";
  String TA_ANALOG_INTENSITY = TA_ANALOG_PREFIX + "-intensity";
  String TA_ANALOG_TIME = TA_ANALOG_PREFIX + "-time";
  String TA_ANALOG_POWER = TA_ANALOG_PREFIX + "-power";
  String TA_ANALOG_ENERGY = TA_ANALOG_PREFIX + "-energy";
  String TA_ANALOG_ELECTRIC_POTENTIAL = TA_ANALOG_PREFIX + "-electric-potential";
  String TA_ANALOG_ELECTRIC_CURRENT = TA_ANALOG_PREFIX + "-electric-current";
  String TA_ANALOG_ELECTRIC_RESISTANCE = TA_ANALOG_PREFIX + "-electric-resistance";
  String TA_ANALOG_SPEED = TA_ANALOG_PREFIX + "-speed";
  String TA_ANALOG_VOLUMETRIC_FLOW_RATE = TA_ANALOG_PREFIX + "-volumetric-flow-rate";
  String TA_ANALOG_PRESSURE = TA_ANALOG_PREFIX + "-pressure";
  String TA_ANALOG_FREQUENCY = TA_ANALOG_PREFIX + "-frequency";
  String TA_ANALOG_ANGLE = TA_ANALOG_PREFIX + "-angle";
  String TA_ANALOG_PULSE = TA_ANALOG_PREFIX + "-pulse";
  String TA_ANALOG_GENERIC = TA_ANALOG_PREFIX + "-generic";
  String TA_DIGITAL_SWITCH = TA_DIGITAL_PREFIX + "-switch";
  String TA_DIGITAL_CONTACT = TA_DIGITAL_PREFIX + "-contact";

  ThingTypeUID TA_UVR_16x2_THING_TYPE = new ThingTypeUID(BINDING_ID, TA_UVR_16x2);

  ThingTypeUID TA_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, TA_DEVICE);

  ChannelTypeUID TA_ANALOG_RAS_MODE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_RAS_MODE);
  ChannelTypeUID TA_ANALOG_RAS_TEMPERATURE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_RAS_TEMPERATURE);

  ChannelTypeUID TA_ANALOG_TEMPERATURE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_TEMPERATURE);
  ChannelTypeUID TA_ANALOG_LENGTH_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_LENGTH);
  ChannelTypeUID TA_ANALOG_VOLUME_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_VOLUME);
  ChannelTypeUID TA_ANALOG_INTENSITY_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_INTENSITY);
  ChannelTypeUID TA_ANALOG_TIME_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_TIME);
  ChannelTypeUID TA_ANALOG_POWER_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_POWER);
  ChannelTypeUID TA_ANALOG_ENERGY_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_ENERGY);
  ChannelTypeUID TA_ANALOG_ELECTRIC_POTENTIAL_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_ELECTRIC_POTENTIAL);
  ChannelTypeUID TA_ANALOG_ELECTRIC_CURRENT_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_ELECTRIC_CURRENT);
  ChannelTypeUID TA_ANALOG_ELECTRIC_RESISTANCE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_ELECTRIC_RESISTANCE);
  ChannelTypeUID TA_ANALOG_SPEED_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_SPEED);
  ChannelTypeUID TA_ANALOG_VOLUMETRIC_FLOW_RATE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_VOLUMETRIC_FLOW_RATE);
  ChannelTypeUID TA_ANALOG_PRESSURE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_PRESSURE);
  ChannelTypeUID TA_ANALOG_FREQUENCY_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_FREQUENCY);
  ChannelTypeUID TA_ANALOG_ANGLE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_ANGLE);
  ChannelTypeUID TA_ANALOG_PULSE_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_PULSE);
  ChannelTypeUID TA_ANALOG_GENERIC_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_GENERIC);

  ChannelTypeUID TA_DIGITAL_SWITCH_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_DIGITAL_SWITCH);
  ChannelTypeUID TA_DIGITAL_CONTACT_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_DIGITAL_CONTACT);

  ThingTypeUID TA_FUNCTION_THING_TYPE = new ThingTypeUID(BINDING_ID, TA_FUNCTION);

  Set<ThingTypeUID> SUPPORTED_DEVICES = new HashSet<>(Arrays.asList(TA_UVR_16x2_THING_TYPE, TA_DEVICE_THING_TYPE));

  Set<ThingTypeUID> DISCOVERABLE_CAN_THINGS = new HashSet<>(Arrays.asList(TA_FUNCTION_THING_TYPE
//    TA_ANALOG_RAS_THING_TYPE, TA_ANALOG_TEMPERATURE_THING_TYPE, TA_ANALOG_LENGTH_THING_TYPE,
//    TA_ANALOG_VOLUME_THING_TYPE, TA_ANALOG_INTENSITY_THING_TYPE, TA_ANALOG_TIME_THING_TYPE, TA_ANALOG_POWER_THING_TYPE,
//    TA_ANALOG_ENERGY_THING_TYPE,
//    TA_ANALOG_ELECTRIC_POTENTIAL_THING_TYPE, TA_ANALOG_ELECTRIC_CURRENT_THING_TYPE, TA_ANALOG_ELECTRIC_RESISTANCE_THING_TYPE,
//    TA_ANALOG_SPEED_THING_TYPE,
//    TA_ANALOG_VOLUMETRIC_FLOW_RATE_THING_TYPE, TA_ANALOG_PRESSURE_THING_TYPE, TA_ANALOG_FREQUENCY_THING_TYPE,
//    TA_ANALOG_PULSE_THING_TYPE, TA_ANALOG_GENERIC_THING_TYPE
  ));

  Set<ThingTypeUID> ALL_SUPPORTED_THINGS = Stream.concat(SUPPORTED_DEVICES.stream(), DISCOVERABLE_CAN_THINGS.stream())
    .collect(Collectors.toSet());

  String TA_ANALOG_OUTPUT = TA_ANALOG_PREFIX + "-output";

  ChannelTypeUID ANALOG_OUTPUT_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, TA_ANALOG_OUTPUT);
  ChannelTypeUID DIGITAL_OUTPUT_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, "ta-digital-output");

}
