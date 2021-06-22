/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.config;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.measure.Dimension;
import javax.measure.Unit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.TAUnits;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.AnalogValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.IntAnalogValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.RASValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.ShortAnalogValue;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import tec.uom.se.AbstractUnit;
import tec.uom.se.spi.Range;

public enum AnalogUnit implements TAUnit {

  /* 0  analog  */ DIMENSIONLESS (0, AbstractUnit.ONE, 1),
  /* 1  analog  */ CELSIUS  (1, SIUnits.CELSIUS, 0.1),
  /* 2  analog  */ IRRADIANCE  (2, Units.IRRADIANCE, 1),
  /* 3  analog  */ LITRE_PER_HOUR (3, Units.LITRE.divide(Units.HOUR), 1),
  /* 4  analog  */ SECOND (4, Units.SECOND, 1),
  /* 5  analog  */ MINUTE (5, Units.MINUTE, 1),
  /* 6  analog  */ LITRE_PER_IMPULSE (6, Units.LITRE.divide(TAUnits.IMPULSE), 1),
  /* 7  analog  */ KELVIN  (7, Units.KELVIN, 1),
  /* 8  analog  */ HUMIDITY  (8, Units.PERCENT, 0.1),
  /* 10 analog  */ KILOWATT (10, Units.WATT.multiply(1000), 0.1),
  /* 11 analog  */ KILOWATT_HOUR (11, Units.KILOWATT_HOUR, 0.1),
  /* 12 analog  */ MEGAWATT_HOUR (12, Units.MEGAWATT_HOUR, 1),
  /* 13 analog  */ VOLT  (13, Units.VOLT, 0.01),
  /* 14 analog  */ MILLI_AMPERE (14, Units.AMPERE.divide(1000), 0.1),
  /* 15 analog  */ HOUR (15, Units.HOUR, 1),
  /* 16 analog  */ DAY (16, Units.DAY, 1),
  /* 17 analog  */ IMPULSE  (17, TAUnits.IMPULSE, 1),
  /* 18 analog  */ KILOOHM (18, MetricPrefix.KILO(Units.OHM), 0.01),
  /* 19 analog  */ LITRE  (19, Units.LITRE, 1),
  /* 20 analog  */ KILOMETRE_PER_HOUR (20, SIUnits.KILOMETRE_PER_HOUR, 1),
  /* 21 analog  */ HERTZ  (21, Units.HERTZ, 1),
  /* 22 analog  */ LITRE_PER_MINUTE  (22, Units.LITRE_PER_MINUTE, 1),
  /* 23 analog  */ BAR  (23, Units.BAR, 0.01),
  /* 24 analog  */ POWER_FACTOR(24, AbstractUnit.ONE, 0.01),
  /* 25 analog  */ KILO_METRE (25, MetricPrefix.KILO(tec.uom.se.unit.Units.METRE), 1),
  /* 26 analog  */ METRE (26, tec.uom.se.unit.Units.METRE, 1),
  /* 27 analog  */ MILLIMETER  (27, MetricPrefix.MILLI(tec.uom.se.unit.Units.METRE), 1),
  /* 28 analog  */ CUBIC_METRE  (28, tec.uom.se.unit.Units.CUBIC_METRE, 1),
  /* 29 analog  */ HERTZ_KM_HOUR  (29, Units.HERTZ.divide(MetricPrefix.KILO(tec.uom.se.unit.Units.METRE).divide(Units.HOUR)), 1),
  /* 30 analog  */ HERTZ_M_SECOND  (30, Units.HERTZ.divide(tec.uom.se.unit.Units.METRE.divide(Units.SECOND)), 1),
  /* 31 analog  */ KILOWATT_PER_IMPULSE  (31, Units.KILOWATT_HOUR.divide(TAUnits.IMPULSE), 1),
  /* 32 analog  */ CUBICMETRE_PER_IMPULSE  (32, tec.uom.se.unit.Units.CUBIC_METRE.divide(TAUnits.IMPULSE), 1),
  /* 33 analog  */ MILLIMETRE_PER_IMPULSE(33, TAUnits.MILIMETRE.divide(TAUnits.IMPULSE), 1),
  /* 34 analog  */ LITER_PER_IMPULSE  (34, Units.LITRE.divide(TAUnits.IMPULSE), 1),
  /* 35 analog  */ LITER_PER_DAY  (35, Units.LITRE.divide(Units.DAY), 1),
  /* 36 analog  */ METRE_PER_SECOND  (36, Units.METRE_PER_SECOND, 1),
  /* 37 analog  */ CUBICMETRE_PER_MINUTE  (37, Units.CUBICMETRE_PER_MINUTE, 1),
  /* 38 analog  */ CUBICMETRE_PER_HOUR  (38, Units.CUBICMETRE_PER_HOUR, 1),
  /* 39 analog  */ CUBICMETRE_PER_DAY  (39, Units.CUBICMETRE_PER_DAY, 1),
  /* 40 analog  */ MILLIMETER_PER_MINUTE  (40, TAUnits.MILIMETRE.divide(Units.MINUTE), 1),
  /* 41 analog  */ MILLIMETER_PER_HOUR  (41, Units.MILLIMETRE_PER_HOUR, 1),
  /* 42 analog  */ MILLIMETER_PER_DAY  (42, TAUnits.MILIMETRE.divide(Units.DAY), 1),
  /* 45 analog  */ RAS_MODE  (45, AbstractUnit.ONE, 1),

//  /* 43 digital */ new DigitalUnit("Aus/Ein", "", "Aus", "Ein"));
//  /* 44 digital */ new DigitalUnit(44, "Nein/Ja", "", "Nein", "Ja"));
//  /* 47 digital */ new DigitalUnit(47, "Stopp/Auf/Zu", "Mischerausgang", "Stopp", "Auf", "Zu"));

  /* 46 analog  */ TEMPERATURE_REGULATOR  (46, SIUnits.CELSIUS, 0.1) {
    @Override
    public ShortAnalogValue parse(short raw) {
      return new RASValue(raw, this);
    }
  }, // complex unit

  /* 54 analog  */ PHASE_SHIFT_DEGREE(54, Units.DEGREE_ANGLE, 0.1),
//  /* 55 digital */ new RollerShutterUnit()); // Jalousie Position für Höhe und Neigung bei Lamelle
//  /* 59 digital */ new ScaledUnit(59, "Prozent", "Jalousie Position", "%", 1, false)); // "Prozent ohne Komma für Jalousie Pos);
//  /* 60 time?   */ new TimeUnit()

  /* 63 analog  */ AMPERE   (63, Units.AMPERE, 0.1),
  /* 65 analog  */ MILLIBAR (65, Units.MILLIBAR, 0.1),
  /* 69 analog  */ WATT   (69, Units.WATT, 1),
  ;

  private final static Map<Integer, AnalogUnit> UNIT_MAP = Arrays.stream(values()).collect(Collectors.toMap(
    AnalogUnit::getIndex,
    value -> value,
    (left, right) -> left
  ));

  private final int index;
  private final Unit<?> unit;
  private final double scale;
  private final Dimension dimension;

  AnalogUnit(int index, Unit<?> unit, double scale) {
    this(index, unit, scale, unit.getDimension());
  }

  AnalogUnit(int index, Unit<?> unit, double scale, Dimension dimension) {
    this.index = index;
    this.unit = unit;
    this.scale = scale;
    this.dimension = dimension;
  }

  public int getIndex() {
    return index;
  }

  public Unit<?> getUnit() {
    return unit;
  }

  public double getScale() {
    return scale;
  }

  public Dimension getDimension() {
    return dimension;
  }

  public static AnalogUnit valueOf(int index) {
    return UNIT_MAP.get(index);
  }

  public ShortAnalogValue parse(short raw) {
    return new ShortAnalogValue(raw, this);
  }

  public IntAnalogValue parse(int raw) {
    return new IntAnalogValue(raw, this);
  }

}
