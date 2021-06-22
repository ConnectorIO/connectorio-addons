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

import java.math.BigDecimal;
import javax.measure.Quantity;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;

public class IntAnalogValue extends BaseAnalogValue<Integer> {

  public IntAnalogValue(BigDecimal value, TAUnit unit) {
    this(value.doubleValue(), unit);
  }

  public IntAnalogValue(Double value, TAUnit unit) {
    this(parse(value, unit), unit);
  }

  public IntAnalogValue(int value, TAUnit unit) {
    this(parse(value, unit), unit);
  }

  public IntAnalogValue(Quantity<?> value, TAUnit unit) {
    super(value, unit);
  }

  public static int parse(Double value, TAUnit unit) {
    if (unit instanceof AnalogUnit) {
      return (int) (value / ((AnalogUnit) unit).getScale());
    }
    return value.intValue();
  }

}
