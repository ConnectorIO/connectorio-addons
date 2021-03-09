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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev;

import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;

class AnalogOutputCallback extends AbstractCallback {

  private final TADevice device;
  private final int offset;

  public AnalogOutputCallback(TADevice device, int offset) {
    this.device = device;
    this.offset = offset;
  }

  @Override
  public void accept(ReadBuffer buffer) throws ParseException {
    for (int index = 1; index < 5; index++) {
      short val = buffer.readShort(16);
      device.updateAnalog(offset + index, val);
    }
  }
}