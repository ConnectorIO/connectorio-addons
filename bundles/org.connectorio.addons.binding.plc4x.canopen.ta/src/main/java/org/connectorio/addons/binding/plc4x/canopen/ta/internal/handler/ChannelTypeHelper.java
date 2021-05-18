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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit.*;
import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ComplexUnit.*;

import java.util.LinkedHashMap;
import java.util.Map;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ComplexUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider.ChannelTypeEntry;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;

@Deprecated
public class ChannelTypeHelper {

  private final static ChannelTypeEntry DEFAULT_ANALOG_OUTPUT = new ChannelTypeEntry(TACANopenBindingConstants.ANALOG_OUTPUT_CHANNEL_TYPE,"Number:Dimensionless");
  private final static ChannelTypeEntry DEFAULT_DIGITAL_OUTPUT = new ChannelTypeEntry(TACANopenBindingConstants.DIGITAL_OUTPUT_CHANNEL_TYPE, "Contact");

  private final static Map<TAUnit, ChannelTypeEntry> unitDimensions = new LinkedHashMap<TAUnit, ChannelTypeEntry>() {{
    put(CELSIUS, new ChannelTypeEntry("temperature", "Number:Temperature"));
    put(KELVIN, new ChannelTypeEntry("temperature", "Number:Temperature"));
    put(KILO_METRE, new ChannelTypeEntry("length", "Number:Length"));
    put(METRE, new ChannelTypeEntry("length", "Number:Length"));
    put(MILLIMETER, new ChannelTypeEntry("length", "Number:Length"));
    put(LITRE, new ChannelTypeEntry("volume", "Number:Volume"));
    put(CUBIC_METRE, new ChannelTypeEntry("volume", "Number:Volume"));
    put(IRRADIANCE, new ChannelTypeEntry("intensity", "Number:Intensity"));
    put(SECOND, new ChannelTypeEntry("time", "Number:Time"));
    put(MINUTE, new ChannelTypeEntry("time", "Number:Time"));
    put(HOUR, new ChannelTypeEntry("time", "Number:Time"));
    put(DAY, new ChannelTypeEntry("time", "Number:Time"));
    put(KILOWATT, new ChannelTypeEntry("power", "Number:Power"));
    put(KILOWATT_HOUR, new ChannelTypeEntry("energy", "Number:Energy"));
    put(MEGAWATT_HOUR, new ChannelTypeEntry("energy", "Number:Energy"));
    put(VOLT, new ChannelTypeEntry("electric-potential", "Number:ElectricPotential"));
    put(MILLI_AMPERE, new ChannelTypeEntry("electric-current", "Number:ElectricCurrent"));
    put(KILOOHM, new ChannelTypeEntry("electric-resistance", "Number:ElectricResistance"));
    put(KILOMETRE_PER_HOUR, new ChannelTypeEntry("speed", "Number:Speed"));
    put(METRE_PER_SECOND, new ChannelTypeEntry("speed", "Number:Speed"));
    put(MILLIMETER_PER_MINUTE, new ChannelTypeEntry("speed", "Number:Speed"));
    put(MILLIMETER_PER_HOUR, new ChannelTypeEntry("speed", "Number:Speed"));
    put(MILLIMETER_PER_DAY, new ChannelTypeEntry("speed", "Number:Speed"));
    put(LITRE_PER_HOUR, new ChannelTypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(LITRE_PER_MINUTE, new ChannelTypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(LITER_PER_DAY, new ChannelTypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(CUBICMETRE_PER_MINUTE, new ChannelTypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(CUBICMETRE_PER_HOUR, new ChannelTypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(CUBICMETRE_PER_DAY, new ChannelTypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(BAR, new ChannelTypeEntry("pressure", "Number:Pressure"));
    put(MILLIBAR, new ChannelTypeEntry("pressure", "Number:Pressure"));
    put(HERTZ, new ChannelTypeEntry("frequency", "Number:Frequency"));
    put(RAS_TEMPERATURE, new ChannelTypeEntry("temperature", "Number:Temperature"));
  }};

  public static ChannelTypeEntry channelType(TAObject object) {
    int unit = object.getUnit();

    DigitalUnit digital = DigitalUnit.valueOf(object.getUnit());
    if (digital != null) {
      return DEFAULT_DIGITAL_OUTPUT;
    }

    AnalogUnit valueType = AnalogUnit.valueOf(unit);
    if (valueType != null) {
      return unitDimensions.getOrDefault(valueType, DEFAULT_ANALOG_OUTPUT);
    }

    ComplexUnit complexType = ComplexUnit.valueOf(unit);
    if (complexType != null) {
      return unitDimensions.getOrDefault(complexType, DEFAULT_ANALOG_OUTPUT);
    }

    return DEFAULT_ANALOG_OUTPUT;
  }

}
