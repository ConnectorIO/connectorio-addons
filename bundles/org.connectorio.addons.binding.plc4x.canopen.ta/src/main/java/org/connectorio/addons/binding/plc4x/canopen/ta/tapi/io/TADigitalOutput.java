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

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.DigitalValue;

public class TADigitalOutput extends TACanOutputObject<DigitalValue> {

  private DigitalValue value;

  public TADigitalOutput(TADevice device, int index, int unit, boolean value) {
    this(device, true, 0x2380, index, unit, value);
  }

  public TADigitalOutput(TADevice device, boolean reload, int index, int unit, boolean value) {
    this(device, reload, 0x2380, index, unit, value);
  }

  TADigitalOutput(TADevice device, boolean reload, int baseIndex, int index, int unit, boolean value) {
    super(device, reload, baseIndex, index, unit, (short) (value ? 1 : 0));
    update(value);
  }

  public void update(boolean value) {
    DigitalUnit digitalUnit = DigitalUnit.valueOf(getUnit());
    if (digitalUnit != null) {
      this.value = new DigitalValue(value, digitalUnit);
    }
  }

  @Override
  public DigitalValue getValue() {
    return value;
  }

}
