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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.i18n;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;

public interface UnitTranslation extends Container {

  Key OFF_ON = new Key("Off / On");
  Key UNIT_NO_YES = new Key("No / Yes");

  Key DIMENSIONLESS = new Key("dimensionless");
  Key DIMENSIONLESS_1 = new Key("Dimensionless (.1)");
  Key PERFORMANCE_FACTOR = new Key("Performance factor");
  Key DIMENSIONLESS_5 = new Key("Dimensionless (.5)");
  Key TEMPERATURE_C = new Key("Temperature °C");
  Key GLOBAL_RADIATION = new Key("Global radiation");
  Key CO2_CONTENT_PPM = new Key("CO2 content (ppm)");
  Key PERCENT = new Key("Percent");
  Key ABSOLUTE_HUMIDITY = new Key("Absolute humidity");
  Key PRESSURE = new Key("Pressure");
  Key PRESSURE_MBAR = new Key("Pressure mbar");
  Key PRESSURE_PASCALS = new Key("Pressure pascals");
  Key FLOW_RATE_LMIN = new Key("Flow rate l/min");
  Key FLOW_RATE_LH = new Key("Flow rate l/h");
  Key FLOW_RATE_LD = new Key("Flow rate l/d");
  Key FLOW_RATE_M3Min = new Key("Flow rate m³/min");
  Key FLOW_RATE_M3H = new Key("Flow rate m³/h");
  Key FLOW_RATE_M3D = new Key("Flow rate m³/d");
  Key OUTPUT_W = new Key("Output W");
  Key OUTPUT_KW = new Key("Output kW");
  Key VOLTAGE = new Key("Voltage");
  Key AMPERAGE_MA = new Key("Amperage mA");
  Key AMPERAGE_A = new Key("Amperage A");
  Key RESISTANCE = new Key("Resistance");
  Key FREQUENCY = new Key("Frequency");
  Key SPEED_KMH = new Key("Speed km/h");
  Key SPEED_MS = new Key("Speed m/s");
  Key DEGREE_ANGLE = new Key("Degree (angle)");

  // Assumed map of units and their handling
  Map<Key, TAUnit> ANALOG_UNIT_MAP = Stream.of(
    new SimpleEntry<>(OFF_ON, DigitalUnit.OFF_ON),
    new SimpleEntry<>(UNIT_NO_YES, DigitalUnit.CLOSE_OPEN),
//
    new SimpleEntry<>(DIMENSIONLESS, AnalogUnit.DIMENSIONLESS),
    new SimpleEntry<>(DIMENSIONLESS_1, AnalogUnit.DIMENSIONLESS),
    new SimpleEntry<>(PERFORMANCE_FACTOR, AnalogUnit.DIMENSIONLESS),
    //new SimpleEntry<>(DIMENSIONLESS_5, AnalogUnit.DIMENSIONLESS),
    new SimpleEntry<>(TEMPERATURE_C, AnalogUnit.CELSIUS),
    //new SimpleEntry<>(GLOBAL_RADIATION, AnalogUnit.DIMENSIONLESS),
    //new SimpleEntry<>(CO2_CONTENT_PPM, AnalogUnit.DIMENSIONLESS),
    //new SimpleEntry<>(PERCENT, AnalogUnit.DIMENSIONLESS),
    new SimpleEntry<>(ABSOLUTE_HUMIDITY, AnalogUnit.HUMIDITY),
    //new SimpleEntry<>(PRESSURE, AnalogUnit.DIMENSIONLESS),
    //new SimpleEntry<>(PRESSURE_MBAR, AnalogUnit.DIMENSIONLESS),
    //new SimpleEntry<>(PRESSURE_PASCALS, AnalogUnit.DIMENSIONLESS),
    new SimpleEntry<>(FLOW_RATE_LMIN, AnalogUnit.LITRE_PER_MINUTE),
    new SimpleEntry<>(FLOW_RATE_LH, AnalogUnit.LITRE_PER_HOUR),
    new SimpleEntry<>(FLOW_RATE_LD, AnalogUnit.LITER_PER_DAY),
    new SimpleEntry<>(FLOW_RATE_M3Min, AnalogUnit.CUBICMETRE_PER_MINUTE),
    new SimpleEntry<>(FLOW_RATE_M3H, AnalogUnit.CUBICMETRE_PER_HOUR),
    new SimpleEntry<>(FLOW_RATE_M3D, AnalogUnit.CUBICMETRE_PER_DAY),
    new SimpleEntry<>(OUTPUT_W, AnalogUnit.WATT),
    new SimpleEntry<>(OUTPUT_KW, AnalogUnit.KILOWATT),
    new SimpleEntry<>(VOLTAGE, AnalogUnit.VOLT),
    new SimpleEntry<>(AMPERAGE_MA, AnalogUnit.MILLI_AMPERE),
    new SimpleEntry<>(AMPERAGE_A, AnalogUnit.AMPERE),
    new SimpleEntry<>(RESISTANCE, AnalogUnit.KILOOHM),
    new SimpleEntry<>(FREQUENCY, AnalogUnit.HERTZ),
    new SimpleEntry<>(SPEED_KMH, AnalogUnit.KILOMETRE_PER_HOUR),
    new SimpleEntry<>(SPEED_MS, AnalogUnit.METRE_PER_SECOND)
    //new SimpleEntry<>(DEGREE_ANGLE, AnalogUnit.DIMENSIONLESS)
  ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

}
