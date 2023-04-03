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
package org.connectorio.addons.binding.wmbus.internal.unit;

import javax.measure.Unit;
import org.openmuc.jmbus.DlmsUnit;

/**
 * Mapping of DLMS units to standardized unit of measurements used in project.
 */
public class DlmsUnits {

  private static String dimension(DlmsUnit unit) {
    switch (unit) {
      case YEAR:
      case MONTH:
      case WEEK:
      case DAY:
      case HOUR:
      case MIN:
      case SECOND:
      case DEGREE:
      case DEGREE_CELSIUS:
      case DEGREE_FAHRENHEIT:
      case CURRENCY:
      case METRE:
      case METRE_PER_SECOND:
      case CUBIC_METRE:
      case CUBIC_METRE_CORRECTED:
      case CUBIC_METRE_PER_HOUR:
      case CUBIC_METRE_PER_HOUR_CORRECTED:
      case CUBIC_METRE_PER_DAY:
      case CUBIC_METRE_PER_DAY_CORRECTED:
      case LITRE:
      case KILOGRAM:
      case NEWTON:
      case NEWTONMETER:
      case PASCAL:
      case BAR:
      case JOULE:
      case JOULE_PER_HOUR:
      case WATT:
      case VOLT_AMPERE:
      case VAR:
      case WATT_HOUR:
      case VOLT_AMPERE_HOUR:
      case VAR_HOUR:
      case AMPERE:
      case COULOMB:
      case VOLT:
      case VOLT_PER_METRE:
      case FARAD:
      case OHM:
      case OHM_METRE:
      case WEBER:
      case TESLA:
      case AMPERE_PER_METRE:
      case HENRY:
      case HERTZ:
      case ACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
      case REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
      case APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
      case VOLT_SQUARED_HOURS:
      case AMPERE_SQUARED_HOURS:
      case KILOGRAM_PER_SECOND:
      case SIEMENS:
      case KELVIN:
      case VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
      case AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
      case METER_CONSTANT_OR_PULSE_VALUE:
      case PERCENTAGE:
      case AMPERE_HOUR:
      case ENERGY_PER_VOLUME:
      case CALORIFIC_VALUE:
      case MOLE_PERCENT:
      case MASS_DENSITY:
      case PASCAL_SECOND:
      case SPECIFIC_ENERGY:
      case SIGNAL_STRENGTH:
      case SIGNAL_STRENGTH_MICROVOLT:
      case LOGARITHMIC:
      case RESERVED:
      case OTHER_UNIT:
      case COUNT:
      case CUBIC_METRE_PER_SECOND:
      case CUBIC_METRE_PER_MINUTE:
      case KILOGRAM_PER_HOUR:
      case CUBIC_FEET:
      case US_GALLON:
      case US_GALLON_PER_MINUTE:
      case US_GALLON_PER_HOUR:
    }
    // TODO add support for UoM
    return null;
  }

  public static Unit<?> valueOf(DlmsUnit unit) {
    return null;
  }

}
