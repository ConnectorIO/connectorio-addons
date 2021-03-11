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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val;

import static org.assertj.core.api.Assertions.assertThat;

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.junit.jupiter.api.Test;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.quantity.Quantities;

class AnalogValueTest {

  @Test
  void testLinearRepresentation() {
    AnalogValue value = new AnalogValue((short) 0x1FF, AnalogUnit.CELSIUS);
    short encode = value.encode();

    AnalogValue value2 = new AnalogValue(encode, AnalogUnit.CELSIUS);
    assertThat(value2).isEqualTo(value);
  }

  @Test
  void testLinearQuantityRepresentation() {
    ComparableQuantity<?> quantity = Quantities.getQuantity(5, AnalogUnit.CELSIUS.getUnit());
    AnalogValue value = new AnalogValue(quantity, AnalogUnit.CELSIUS);
    short encode = value.encode();

    AnalogValue value2 = new AnalogValue(encode, AnalogUnit.CELSIUS);
    assertThat(value2).isEqualTo(value);
  }

}