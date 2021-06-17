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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val;

import java.util.Objects;
import javax.measure.Quantity;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import tec.uom.se.quantity.Quantities;

public abstract class BaseAnalogValue<T extends Number> implements Value<Quantity<?>> {

  private final Quantity<?> value;
  private final TAUnit unit;

  public BaseAnalogValue(Quantity<?> value, TAUnit unit) {
    this.value = value;
    this.unit = unit;
  }

  public Quantity<?> getValue() {
    return value;
  }

  @Override
  public TAUnit getUnit() {
    return unit;
  }

  public T encode() {
    if (unit instanceof AnalogUnit) {
      AnalogUnit taUnit = (AnalogUnit) unit;
      return cast((value.getValue().doubleValue() / taUnit.getScale()));
    }

    return null;
  }

  protected abstract T cast(double value);

  public String toString() {
    return getClass().getSimpleName() + " [" + value + ", unit=" + unit + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseAnalogValue)) {
      return false;
    }
    BaseAnalogValue that = (BaseAnalogValue) o;
    return Objects.equals(getValue(), that.getValue()) && Objects.equals(getUnit(), that.getUnit());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue(), getUnit());
  }

  protected static Quantity<?> parse(int raw, TAUnit unit) {
    if (unit instanceof AnalogUnit) {
      AnalogUnit analog = (AnalogUnit) unit;
      final double value = raw * analog.getScale();
      return Quantities.getQuantity(value, analog.getUnit());
    }

    // shall we log it?
    throw new IllegalArgumentException("Unknown unit " + unit + ", can not be parsed as analog");
  }

}
