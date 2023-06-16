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

import java.util.Objects;
import javax.measure.Quantity;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.internal.type.TAUnit;
import tech.units.indriya.quantity.Quantities;

public class RASValue extends ShortAnalogValue {

  private final int mode;

  public RASValue(short value, TAUnit unit) {
    this(parse(value), parseMode(value), unit);
  }

  public RASValue(Quantity<?> value, int mode, TAUnit unit) {
    super(value, unit);
    this.mode = mode;
  }

  @Override
  public short encode() {
    double value = getValue().getValue().doubleValue();

    int val = (int) (Math.abs(value) / 0.1);
    if (value < 0) {
      // set 15-th bit (on left, counting from 0)
      val |= 0x8000;
    }
    if (value > 0) {
      // set 14 th bit (on left, counting from 0)
      val |= 0x4000;
    }

    return (short) ((val) | mode << 9);
  }

  public int getMode() {
    return mode;
  }

  public String toString() {
    return "RASValue [" + getValue() + ", mode=" + mode + ", unit=" + getUnit() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RASValue)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    RASValue rasValue = (RASValue) o;
    return getMode() == rasValue.getMode();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getMode());
  }

  private static Quantity<?> parse(short raw) {
    boolean negative = (raw & 0x8000) != 0;

    double value = 0.1 * (raw & 0x1FF) * (negative ? -1 : 1);
    return Quantities.getQuantity(value, AnalogUnit.CELSIUS.getUnit());
  }

  private static int parseMode(short raw) {
    return (raw & 0x600) >> 9;
  }

}
