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

import org.apache.commons.codec.binary.Hex;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.i18n.Language;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;

public class TAFixedValueObject<T extends Value<?>> extends TACanInputOutputObject<T> {

  private T value;

  public TAFixedValueObject(TADevice device, int index, int unit) {
    this(device, true, 0x2400, index, unit);
  }

  TAFixedValueObject(TADevice device, boolean reload, int baseIndex, int index, int unit) {
    super(device, reload, baseIndex, index, unit);
  }

  @Override
  protected void reload() {
    getName().whenComplete((name, failure) -> {
      if (failure != null) {
        return;
      }

      if (device.getLanguage().matches(Language.UNUSED, name)) {
        return;
      }

      // 2414 -> value
      device.getNode().read((short) 0x2414, (short) (index - 1)).whenComplete((value, fail) -> {
        logger.info("Present value {}", Hex.encodeHexString(value), fail);
      });

      // 2415 -> minimum
      // 2416 -> maximum
    });
  }

  @Override
  public T getValue() {
    return value;
  }

}
