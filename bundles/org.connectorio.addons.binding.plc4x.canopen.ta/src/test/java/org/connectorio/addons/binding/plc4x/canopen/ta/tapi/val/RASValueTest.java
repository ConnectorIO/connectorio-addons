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

import java.util.Arrays;
import java.util.List;
import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import org.assertj.core.data.Percentage;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

class RASValueTest {

  @MethodSource
  @ParameterizedTest
  void verifyRasValue(Argument argument) {
    RASValue rasValue = new RASValue(argument.raw, AnalogUnit.TEMPERATURE_REGULATOR);

    Quantity<Temperature> quantity = Quantities.getQuantity(argument.temperature, Units.CELSIUS);
    assertThat(rasValue.getValue().getValue().doubleValue())
      .isCloseTo(quantity.getValue().doubleValue(), Percentage.withPercentage(1));
    assertThat(rasValue.getMode()).isEqualTo(argument.mode);

    short encoded = rasValue.encode();
    assertThat(Integer.toHexString(encoded))
      .isEqualTo(Integer.toHexString(argument.raw));
  }

  private static List<Argument> verifyRasValue() {
    return Arrays.asList(
      new Argument(0x40c6, 0, 19.8),
      new Argument(0x46ea, 3, 23.4),
      new Argument(0x40c3, 0, 19.5),
      new Argument(0x40af, 0, 17.5),
      new Argument(0x0600, 3, 0),
      new Argument(0x0400, 2, 0),
      new Argument(0x0200, 1, 0),
      new Argument(0x0000, 0, 0),
      new Argument(0x4001, 0, 0.1),
      new Argument(0x4080, 0, 12.8),
      new Argument(0x8080, 0, -12.8)
    );
  }

  static class Argument {
    final short raw, mode;
    final double temperature;

    Argument(int raw, int mode, double temperature) {
      this((short) raw, (short) mode, temperature);
    }

    Argument(short raw, short mode, double temperature) {
      this.raw = raw;
      this.mode = mode;
      this.temperature = temperature;
    }

    @Override
    public String toString() {
      return "0x" + Integer.toHexString(raw) + ", mode=" + mode + ", temperature=" + temperature;
    }
  }

}