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
package org.connectorio.addons.binding.canopen.ta.tapi.io;

import org.connectorio.addons.binding.canopen.ta.tapi.TACanStringPointer;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.canopen.ta.tapi.val.Value;

public abstract class TACanOutputObject<T extends Value> extends TACanInputOutputObject<T> {

  private TACanStringPointer sourceType;
  private TACanStringPointer sourceObject;
  private TACanStringPointer sourceVariable;

  public TACanOutputObject(TADevice device, boolean reload, int baseIndex, int index, int unit, short value) {
    super(device, reload, baseIndex, index, unit);
  }

  @Override
  protected void reload() {
    this.sourceType = new TACanStringPointer(device.getNode(), baseIndex + 0x2000 + 0x50, index - 1);
    this.sourceObject = new TACanStringPointer(device.getNode(), baseIndex + 0x2000 + 0x51, index - 1);
    this.sourceVariable = new TACanStringPointer(device.getNode(), baseIndex + 0x2000 + 0x52, index - 1);
  }
}
