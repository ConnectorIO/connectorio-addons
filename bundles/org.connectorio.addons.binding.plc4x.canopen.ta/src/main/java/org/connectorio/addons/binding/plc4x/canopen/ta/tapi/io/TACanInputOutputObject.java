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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TACanString;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TACanStringPointer;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;

public abstract class TACanInputOutputObject<T extends Value> {

  private final TADevice device;
  private final int baseIndex;
  private final int index;
  private final int unit;

  private TACanString name;
  private TACanStringPointer sourceType;
  private TACanStringPointer sourceObject;
  private TACanStringPointer sourceVariable;

  public TACanInputOutputObject(TADevice device, boolean reload, int index, int unit) {
    this(device, reload, 0x0000, index, unit);
  }

  public TACanInputOutputObject(TADevice device, boolean reload, int baseIndex, int index, int unit) {
    this.device = device;
    this.baseIndex = baseIndex;
    this.index = index;
    this.unit = unit;

    if (reload) {
      this.name = new TACanString(device.getNode(), (short) (baseIndex + 0x0F), (short) (index - 1));
      this.sourceType = new TACanStringPointer(device.getNode(), baseIndex + 0x2000 + 0x50, index - 1);
      this.sourceObject = new TACanStringPointer(device.getNode(), baseIndex + 0x2000 + 0x51, index - 1);
      this.sourceVariable = new TACanStringPointer(device.getNode(), baseIndex + 0x2000 + 0x52, index - 1);
    }
  }

  public CompletableFuture<String> getName() {
    return name.toFuture();
  }

  public int getIndex() {
    return index;
  }

  public int getUnit() {
    return unit;
  }

  public String toString() {
    return getClass().getSimpleName() + "[" + index + "] unit=" + unit + ", name=" + name;
  }

  public abstract T getValue();
}
