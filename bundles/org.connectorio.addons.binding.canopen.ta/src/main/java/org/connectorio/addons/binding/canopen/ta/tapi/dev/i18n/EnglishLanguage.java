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
package org.connectorio.addons.binding.canopen.ta.tapi.dev.i18n;

import java.util.ArrayList;
import java.util.List;

public class EnglishLanguage implements Language, UnitTranslation {

  private static List<Key> TRANSLATION = new ArrayList<Key>() {
    {{
      add(UNUSED);
      add(AUTOMATIC);
      add(USER_DEFINED);
      add(YES);
      add(NO);

      // units
      // - digital
      add(OFF_ON);
      add(UNIT_NO_YES);
      // - analog
      add(DIMENSIONLESS);
      add(DIMENSIONLESS_1);
      add(PERFORMANCE_FACTOR);
      add(DIMENSIONLESS_5);
      add(TEMPERATURE_C);
      add(GLOBAL_RADIATION);
      add(CO2_CONTENT_PPM);
      add(PERCENT);
      add(ABSOLUTE_HUMIDITY);
      add(PRESSURE);
      add(PRESSURE_MBAR);
      add(PRESSURE_PASCALS);
      add(FLOW_RATE_LMIN);
      add(FLOW_RATE_LH);
      add(FLOW_RATE_LD);
      add(FLOW_RATE_M3Min);
      add(FLOW_RATE_M3H);
      add(FLOW_RATE_M3D);
      add(OUTPUT_W);
      add(OUTPUT_KW);
      add(VOLTAGE);
      add(AMPERAGE_MA);
      add(AMPERAGE_A);
      add(RESISTANCE);
      add(FREQUENCY);
      add(SPEED_KMH);
      add(SPEED_MS);
      add(DEGREE_ANGLE);
    }}
  };

  @Override
  public boolean matches(Key key, String text) {
    return key.label().equals(text);
  }

  @Override
  public Key lookup(String text) {
    return TRANSLATION.stream()
      .filter(key -> key.label().equals(text))
      .findFirst().orElse(null);
  }

  @Override
  public UnitTranslation getUnits() {
    return this;
  }

}
