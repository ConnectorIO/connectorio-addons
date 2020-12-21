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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.config;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.measure.Dimension;
import javax.measure.Unit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.TAUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.Units;

public enum AnalogUnit implements TAUnit {

    /* 0  analog  */ DIMENSIONLESS (0, AbstractUnit.ONE, 1),
    /* 1  analog  */ CELSIUS  (1, SIUnits.CELSIUS, 0.1),
    /* 2  analog  */ IRRADIANCE  (2, SmartHomeUnits.IRRADIANCE, 1),
    /* 3  analog  */ LITRE_PER_HOUR (3, SmartHomeUnits.LITRE.divide(SmartHomeUnits.HOUR), 1),
    /* 4  analog  */ SECOND (4, SmartHomeUnits.SECOND, 1),
    /* 5  analog  */ MINUTE (5, SmartHomeUnits.MINUTE, 1),
    /* 6  analog  */ LITRE_PER_IMPULSE (6, SmartHomeUnits.LITRE.divide(TAUnits.IMPULSE), 1),
    /* 7  analog  */ KELVIN  (7, Units.KELVIN, 1),
    /* 8  analog  */ HUMIDITY  (8, SmartHomeUnits.PERCENT, 1),
    /* 10 analog  */ KILOWATT (10, SmartHomeUnits.WATT.multiply(1000), 0.1),
    /* 11 analog  */ KILOWATT_HOUR (11, SmartHomeUnits.KILOWATT_HOUR, 1),
    /* 12 analog  */ MEGAWATT_HOUR (12, SmartHomeUnits.MEGAWATT_HOUR, 1),
    /* 13 analog  */ VOLT  (13, SmartHomeUnits.VOLT, 0.01),
    /* 14 analog  */ MILLI_AMPERE (14, SmartHomeUnits.AMPERE.divide(1000), 0.1),
    /* 15 analog  */ HOUR (15, SmartHomeUnits.HOUR, 1),
    /* 16 analog  */ DAY (16, SmartHomeUnits.DAY, 1),
    /* 17 analog  */ IMPULSE  (17, TAUnits.IMPULSE, 1),
    /* 18 analog  */ KILOOHM (18, MetricPrefix.KILO(SmartHomeUnits.OHM), 0.01),
    /* 19 analog  */ LITRE  (19, SmartHomeUnits.LITRE, 1),
    /* 20 analog  */ KILOMETRE_PER_HOUR (20, SIUnits.KILOMETRE_PER_HOUR, 1),
    /* 21 analog  */ HERTZ  (21, SmartHomeUnits.HERTZ, 1),
    /* 22 analog  */ LITRE_PER_MINUTE  (22, SmartHomeUnits.LITRE_PER_MINUTE, 1),
    /* 23 analog  */ BAR  (23, SmartHomeUnits.BAR, 0.01),
    /* 25 analog  */ KILO_METRE (25, MetricPrefix.KILO(Units.METRE), 1),
    /* 26 analog  */ METRE (26, Units.METRE, 1),
    /* 27 analog  */ MILLIMETER  (27, MetricPrefix.MILLI(Units.METRE), 1),
    /* 28 analog  */ CUBIC_METRE  (28, Units.CUBIC_METRE, 1),
    /* 29 analog  */ HERTZ_KM_HOUR  (29, SmartHomeUnits.HERTZ.divide(MetricPrefix.KILO(Units.METRE).divide(SmartHomeUnits.HOUR)), 1),
    /* 30 analog  */ HERTZ_M_SECOND  (30, SmartHomeUnits.HERTZ.divide(Units.METRE.divide(SmartHomeUnits.SECOND)), 1),
    /* 31 analog  */ KILOWATT_PER_IMPULSE  (31, SmartHomeUnits.KILOWATT_HOUR.divide(TAUnits.IMPULSE), 1),
    /* 32 analog  */ CUBICMETRE_PER_IMPULSE  (32, Units.CUBIC_METRE.divide(TAUnits.IMPULSE), 1),
    /* 33 analog  */ MILLIMETRE_PER_IMPULSE(33, TAUnits.MILIMETRE.divide(TAUnits.IMPULSE), 1),
    /* 34 analog  */ LITER_PER_IMPULSE  (34, SmartHomeUnits.LITRE.divide(TAUnits.IMPULSE), 1),
    /* 35 analog  */ LITER_PER_DAY  (35, SmartHomeUnits.LITRE.divide(SmartHomeUnits.DAY), 1),
    /* 36 analog  */ METRE_PER_SECOND  (36, SmartHomeUnits.METRE_PER_SECOND, 1),
    /* 37 analog  */ CUBICMETRE_PER_MINUTE  (37, SmartHomeUnits.CUBICMETRE_PER_MINUTE, 1),
    /* 38 analog  */ CUBICMETRE_PER_HOUR  (38, SmartHomeUnits.CUBICMETRE_PER_HOUR, 1),
    /* 39 analog  */ CUBICMETRE_PER_DAY  (39, SmartHomeUnits.CUBICMETRE_PER_DAY, 1),
    /* 40 analog  */ MILLIMETER_PER_MINUTE  (40, TAUnits.MILIMETRE.divide(SmartHomeUnits.MINUTE), 1),
    /* 41 analog  */ MILLIMETER_PER_HOUR  (41, SmartHomeUnits.MILLIMETRE_PER_HOUR, 1),
    /* 42 analog  */ MILLIMETER_PER_DAY  (42, TAUnits.MILIMETRE.divide(SmartHomeUnits.DAY), 1),

//    /* 43 digital */ new DigitalUnit("Aus/Ein", "", "Aus", "Ein"));
//    /* 44 digital */ new DigitalUnit(44, "Nein/Ja", "", "Nein", "Ja"));
//    /* 47 digital */ new DigitalUnit(47, "Stopp/Auf/Zu", "Mischerausgang", "Stopp", "Auf", "Zu"));
//    /* 55 digital */ new RollerShutterUnit()); // Jalousie Position für Höhe und Neigung bei Lamelle
//    /* 59 digital */ new ScaledUnit(59, "Prozent", "Jalousie Position", "%", 1, false)); // "Prozent ohne Komma für Jalousie Pos);
//    /* 60 time?   */ new TimeUnit()
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

}
