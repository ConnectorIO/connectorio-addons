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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io;

import java.util.List;
import javax.measure.Quantity;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ComplexUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.RASValue;

public class TARasOutput extends TAAnalogOutput {

  private RASValue value;

  public TARasOutput(TADevice device, int index, int unit, short value) {
    super(device, index, unit, value);
  }

  @Override
  public void update(short raw) {
    ComplexUnit complexUnit = ComplexUnit.valueOf(getUnit());
    if (complexUnit != null) {
      List<Object> value = complexUnit.parse(raw);
      this.value = new RASValue((Quantity<?>) value.get(0), (int) value.get(1), complexUnit);
    }
  }

  @Override
  public RASValue getValue() {
    return value;
  }

}
