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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit.*;
import static org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ComplexUnit.*;

import java.util.LinkedHashMap;
import java.util.Map;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ComplexUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;

public class ChannelTypeHelper {

  private final static TypeEntry DEFAULT_ANALOG_OUTPUT = new TypeEntry(TACANopenBindingConstants.ANALOG_OUTPUT_CHANNEL_TYPE,"Number:Dimensionless");
  private final static TypeEntry DEFAULT_DIGITAL_OUTPUT = new TypeEntry(TACANopenBindingConstants.DIGITAL_OUTPUT_CHANNEL_TYPE, "Contact");

  private final static Map<TAUnit, TypeEntry> unitDimensions = new LinkedHashMap<TAUnit, TypeEntry>() {{
    put(CELSIUS, new TypeEntry("temperature", "Number:Temperature"));
    put(KELVIN, new TypeEntry("temperature", "Number:Temperature"));
    put(KILO_METRE, new TypeEntry("length", "Number:Length"));
    put(METRE, new TypeEntry("length", "Number:Length"));
    put(MILLIMETER, new TypeEntry("length", "Number:Length"));
    put(LITRE, new TypeEntry("volume", "Number:Volume"));
    put(CUBIC_METRE, new TypeEntry("volume", "Number:Volume"));
    put(IRRADIANCE, new TypeEntry("intensity", "Number:Intensity"));
    put(SECOND, new TypeEntry("time", "Number:Time"));
    put(MINUTE, new TypeEntry("time", "Number:Time"));
    put(HOUR, new TypeEntry("time", "Number:Time"));
    put(DAY, new TypeEntry("time", "Number:Time"));
    put(KILOWATT, new TypeEntry("power", "Number:Power"));
    put(KILOWATT_HOUR, new TypeEntry("energy", "Number:Energy"));
    put(MEGAWATT_HOUR, new TypeEntry("energy", "Number:Energy"));
    put(VOLT, new TypeEntry("electric-potential", "Number:ElectricPotential"));
    put(MILLI_AMPERE, new TypeEntry("electric-current", "Number:ElectricCurrent"));
    put(KILOOHM, new TypeEntry("electric-resistance", "Number:ElectricResistance"));
    put(KILOMETRE_PER_HOUR, new TypeEntry("speed", "Number:Speed"));
    put(METRE_PER_SECOND, new TypeEntry("speed", "Number:Speed"));
    put(MILLIMETER_PER_MINUTE, new TypeEntry("speed", "Number:Speed"));
    put(MILLIMETER_PER_HOUR, new TypeEntry("speed", "Number:Speed"));
    put(MILLIMETER_PER_DAY, new TypeEntry("speed", "Number:Speed"));
    put(LITRE_PER_HOUR, new TypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(LITRE_PER_MINUTE, new TypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(LITER_PER_DAY, new TypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(CUBICMETRE_PER_MINUTE, new TypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(CUBICMETRE_PER_HOUR, new TypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(CUBICMETRE_PER_DAY, new TypeEntry("volumetric-flow-rate", "Number:VolumetricFlowRate"));
    put(BAR, new TypeEntry("pressure", "Number:Pressure"));
    put(MEGABAR, new TypeEntry("pressure", "Number:Pressure"));
    put(HERTZ, new TypeEntry("frequency", "Number:Frequency"));
    put(RAS_TEMPERATURE, new TypeEntry("temperature", "Number:Temperature"));
  }};

  public static TypeEntry channelType(TAObject object) {
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
