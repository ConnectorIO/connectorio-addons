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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io;

import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.i18n.Key;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.i18n.UnitTranslation;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.AnalogValue;

public class TAAnalogInput extends TACanInputObject<AnalogValue> {

  private AnalogValue value;

  public TAAnalogInput(TADevice device, int index, int unit) {
    this(device, 0x2200, index, unit);
  }

  TAAnalogInput(TADevice device, int baseIndex, int index, int unit) {
    this(device, true, baseIndex, index, unit);
  }

  TAAnalogInput(TADevice device, boolean reload, int baseIndex, int index, int unit) {
    super(device, reload, baseIndex, index, unit);
  }

  @Override
  protected int parseUnitLabel(String unitLabel) {
    // lookup key which matches given label
    Key unitLanguageKey = device.getLanguage().getUnits().lookup(unitLabel);
    if (UnitTranslation.ANALOG_UNIT_MAP.containsKey(unitLanguageKey)) {
      // check if label is mapped to any unit
      return UnitTranslation.ANALOG_UNIT_MAP.get(unitLanguageKey).getIndex();
    }

    // not known
    return InputConfig.UNIT_USER_DEFINED;
  }

  @Override
  public AnalogValue getValue() {
    return this.value;
  }

  public void update(AnalogValue value) {
    this.value = value;
  }

}
