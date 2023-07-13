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
package org.connectorio.addons.binding.canopen.ta.tapi.func;

import org.connectorio.addons.binding.canopen.ta.tapi.TACanString;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;

public class TAFunction {

  private final TADevice device;
  private final int index;
  private final TACanString type;
  private final TACanString name;

  public TAFunction(TADevice device, int index) {
    this.device = device;
    this.index = index;

    this.type = new TACanString(device.getNode(), (short) 0x2800, (short) (index - 1));
    this.name = new TACanString(device.getNode(), (short) 0x2805, (short) (index - 1));
  }

}
