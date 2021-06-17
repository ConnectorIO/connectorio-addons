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

import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.BaseAnalogValue;

public class TAAnalogOutput extends TACanOutputObject<BaseAnalogValue<?>> {

  private BaseAnalogValue<? extends Number> value;
  // force initial type to be Int to retrieve actual type used by controller via SDO request,
  // when actual type differs we will learn that from SDO answer, type will be swapped
  private int type = 0x50;

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
    if (type != 0x50) { // value kept by controller is not encoded as Int
      this.value = AnalogUnit.valueOf(getUnit()).parse(raw);
    } else {
      fetch();
    }
  }

  void fetch() {
    device.getNode().read((short) (0x2280 + 0x55), (short) (index - 1)).thenAccept(this::update);
  }

  void update(byte[] data) {
    try {
      ReadBuffer buffer = new ReadBuffer(data, true);
      byte type = buffer.readByte(8); // type
      TAAnalogOutput.this.unit = buffer.readByte(8); // unit
      int numericValue = parseNumber(type, data, buffer);

      this.type = type;
      this.value = AnalogUnit.valueOf(getUnit()).parse(numericValue);
      logger.debug("Encoded value for CAN Output {} is {}, decoded as {}", TAAnalogOutput.this, Hex.encodeHexString(data), numericValue);
    } catch (Exception e) {
      logger.error("Failed to parse answer", e);
    }
  }

  private int parseNumber(byte type, byte[] data, ReadBuffer buffer) throws ParseException {
    if (type == 0x50) {
      return buffer.readInt(32);
    }
    if (data.length == 4) { // type + unit + short
      return buffer.readShort(16);
    }

    return buffer.readInt(32);
  }

  @Override
  public BaseAnalogValue<?> getValue() {
    return value;
  }

}
