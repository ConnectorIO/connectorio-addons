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
package org.connectorio.addons.binding.canopen.ta.internal.type;

import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.internal.config.ComplexUnit;
import org.connectorio.addons.binding.canopen.ta.internal.config.DigitalUnit;
import tec.uom.se.quantity.Quantities;

public class TAValue {

  // both are shorts but for ease of compilation we mark'em as ints
  /**
   * TA specific unit index.
   */
  private final int index;

  /**
   * Raw value transmitted over CAN.
   */
  private final int raw;

  public TAValue(int index, int raw) {
    this.index = index;
    this.raw = raw;
  }

  public Object getValue() {
    DigitalUnit unit = DigitalUnit.valueOf(index);
    if (unit != null) {
      // TODO this logic requires additional verification
      //int value = Short.toUnsignedInt(Short.valueOf((short) raw));
      return unit.parse(raw);
    }

    // must be checked before analog since complex unit here overrides standard analog decoding logic.
    final ComplexUnit complex = ComplexUnit.valueOf(index);
    if (complex != null) {
      return complex.parse(raw);
    }

    final AnalogUnit analog = AnalogUnit.valueOf(index);
    if (analog != null) {
      final double value = raw * analog.getScale();
      return Quantities.getQuantity(value, analog.getUnit());
    }

    return raw;
  }

  @Override
  public String toString() {
    return "TAValue [unit=" + index + ",raw=" + raw + "; " + getValue() + "]";
  }
}
