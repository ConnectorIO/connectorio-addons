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

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.AnalogValue;
import tec.uom.se.quantity.Quantities;

public class TAAnalogOutput extends TACanOutputObject<AnalogValue> {

  private AnalogValue value;

  public TAAnalogOutput(TADevice device, int index, int unit, short value) {
    this(device, true, 0x2280, index, unit, value);
  }

  public TAAnalogOutput(TADevice device, boolean reload, int index, int unit, short value) {
    this(device, reload, 0x2280, index, unit, value);
  }

  protected TAAnalogOutput(TADevice device, boolean reload, int baseIndex, int index, int unit, short value) {
    super(device, reload, baseIndex, index, unit, value);
    update(value);
  }

  public void update(short raw) {
    this.value = AnalogUnit.valueOf(getUnit()).parse(raw);
  }

  @Override
  public AnalogValue getValue() {
    return value;
  }

}
