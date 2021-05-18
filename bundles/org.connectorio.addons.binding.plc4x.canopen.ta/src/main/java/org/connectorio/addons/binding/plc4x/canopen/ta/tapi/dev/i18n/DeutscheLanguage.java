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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.i18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DeutscheLanguage implements Language, UnitTranslation {

  private static Map<Key, String> TRANSLATION = new HashMap<Key, String>() {
    {{
      put(UNUSED, "unbenutzt");
      put(AUTOMATIC, "Automatisch");
      put(USER_DEFINED, "Benutzerdefiniert");
      put(YES, "Ja");
      put(NO, "Nein");

      // units
      // - digital
      put(OFF_ON, "Aus / Ein");
      put(UNIT_NO_YES, "Nein / Ja");
      // - analog
      put(DIMENSIONLESS, "dimensionslos");
      put(DIMENSIONLESS_1, "dimensionslos (,1)");
      put(PERFORMANCE_FACTOR, "Arbeitszahl");
      put(DIMENSIONLESS_5, "dimensionslos (,5)");
      put(TEMPERATURE_C, "Temperatur °C");
      put(GLOBAL_RADIATION, "Globalstrahlung");
      put(CO2_CONTENT_PPM, "CO2-Gehalt ppm");
      put(PERCENT, "Prozent");
      put(ABSOLUTE_HUMIDITY, "Absolute Feuchte");
      put(PRESSURE, "Druck bar");
      put(PRESSURE_MBAR, "Druck mbar");
      put(PRESSURE_PASCALS, "Druck Pascal");
      put(FLOW_RATE_LMIN, "Durchfluss l/min");
      put(FLOW_RATE_LH, "Durchfluss l/h");
      put(FLOW_RATE_LD, "Durchfluss l/d");
      put(FLOW_RATE_M3Min, "Durchfluss m³/min");
      put(FLOW_RATE_M3H, "Durchfluss m³/h");
      put(FLOW_RATE_M3D, "Durchfluss m³/d");
      put(OUTPUT_W, "Leistung W");
      put(OUTPUT_KW, "Leistung kW");
      put(VOLTAGE, "Spannung");
      put(AMPERAGE_MA, "Stromstärke mA");
      put(AMPERAGE_A, "Stromstärke A");
      put(RESISTANCE, "Widerstand");
      put(FREQUENCY, "Frequenz");
      put(SPEED_KMH, "Geschwindigkeit km/h");
      put(SPEED_MS, "Geschwindigkeit m/s");
      put(DEGREE_ANGLE, "Grad (Winkel)");
    }}
  };

  @Override
  public boolean matches(Key key, String text) {
    if (TRANSLATION.containsKey(key)) {
      return TRANSLATION.get(key).equals(text);
    }

    return false;
  }

  @Override
  public Key lookup(String text) {
    return TRANSLATION.entrySet().stream()
      .filter(entry -> entry.getValue().equals(text))
      .map(Entry::getKey)
      .findFirst().orElse(null);
  }

  @Override
  public UnitTranslation getUnits() {
    return this;
  }

}
