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
package org.connectorio.addons.binding.canopen.ta.tapi.val;

import static org.assertj.core.api.Assertions.assertThat;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import org.assertj.core.data.Percentage;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.unit.Units;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.quantity.Quantities;

class AnalogValueTest {

  @Test
  void testLinearRepresentation() {
    ShortAnalogValue value = new ShortAnalogValue((short) 0x1FF, AnalogUnit.CELSIUS);
    short encode = value.encode();

    ShortAnalogValue value2 = new ShortAnalogValue(encode, AnalogUnit.CELSIUS);
    assertThat(value2).isEqualTo(value);
  }

  @Test
  void testElectricityReading() {
    IntAnalogValue value = new IntAnalogValue(0x016fbc, AnalogUnit.KILOWATT_HOUR);

    Quantity<Energy> quantity = Quantities.getQuantity(9414.0, Units.KILOWATT_HOUR);
    assertThat(value.getValue().getValue().doubleValue())
      .isCloseTo(quantity.getValue().doubleValue(), Percentage.withPercentage(1));
  }

  @Test
  void testElectricityRepresentation() {
    IntAnalogValue value = new IntAnalogValue(Integer.reverseBytes(0x27eb0000), AnalogUnit.KILOWATT_HOUR);
    short encode = value.encode();

    // values encoded for PDOs are subject of data truncation to two bytes
    IntAnalogValue value2 = new IntAnalogValue(encode, AnalogUnit.KILOWATT_HOUR);
    assertThat(value2).isNotEqualTo(value);
  }

  @Test
  void testLinearQuantityRepresentation() {
    ComparableQuantity<?> quantity = Quantities.getQuantity(5, AnalogUnit.CELSIUS.getUnit());
    ShortAnalogValue value = new ShortAnalogValue(quantity, AnalogUnit.CELSIUS);
    short encode = value.encode();

    ShortAnalogValue value2 = new ShortAnalogValue(encode, AnalogUnit.CELSIUS);
    assertThat(value2).isEqualTo(value);
  }

}