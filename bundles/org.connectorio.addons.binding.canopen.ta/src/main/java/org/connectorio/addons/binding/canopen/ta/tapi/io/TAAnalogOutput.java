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

import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.canopen.ta.tapi.val.BaseAnalogValue;
import org.connectorio.addons.binding.canopen.ta.tapi.val.ShortAnalogValue;
import org.connectorio.addons.binding.canopen.ta.tapi.val.Value;

public class TAAnalogOutput extends TACanOutputObject<Value<?>> {

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

  public void update(Value<?> value) {
    this.value = (BaseAnalogValue<? extends Number>) value;
  }

  public void update(short raw) {
    if (device.isInitialized()) {
      if (type != 0x50) { // value kept by controller is not encoded as Int
        AnalogUnit unit = AnalogUnit.valueOf(getUnit());
        if (unit != null) {
          // unit can be null for digital encoded as analog
          this.value = unit.parse(raw);
        }
      } else {
        fetch();
      }
    }
  }

  void fetch() {
    device.getNode().read((short) (0x2280 + 0x55), (short) (index - 1)).thenAccept(this::update);
  }

  void update(byte[] data) {
    try {
      logger.trace("Encoded value for CAN Output {} is {}", this, Hex.encodeHexString(data));
      ReadBuffer buffer = new ReadBuffer(data, true);
      this.type = buffer.readByte(8); // type
      this.unit = buffer.readByte(8); // unit
      int numericValue = parseNumber(buffer, data.length);

      if (numericValue > Short.MIN_VALUE && numericValue < Short.MAX_VALUE) {
        // Here we verify if unit is a counter "type" for energy or power measurement.
        // We can't know for sure what it is, so we take preventive measure.
        // For units listed below we will use fetch calls and retrieve data over SDO.
        // At this stage it is being made for kWh, W and kW are units which can exceed 32767
        if (AnalogUnit.KILOWATT_HOUR.getIndex() != unit && AnalogUnit.WATT.getIndex() != unit && AnalogUnit.KILOWATT.getIndex() != unit) {
          // force handling value as a short.
          this.type = 0x30;
        }
      }

      AnalogUnit analogUnit = AnalogUnit.valueOf(getUnit());
      if (analogUnit != null) {
        this.value = analogUnit.parse(numericValue);
        logger.debug("Encoded value of CAN Output {}, decoded as {}, parsed to {}", this, numericValue, value);
        return;
      }

      // most likely a digital unit which is written using analog channel
      this.value = new ShortAnalogValue(Integer.valueOf(numericValue).shortValue(), AnalogUnit.DIMENSIONLESS);
    } catch (Exception e) {
      logger.error("Failed to parse answer", e);
    }
  }

  private int parseNumber(ReadBuffer buffer, int length) throws ParseException {
    if (length == 2) {
      return 0;
    }
    if (type == 0x50 && length >= 6) {
      return buffer.readInt(32);
    }
    if (length >= 4) { // type + unit + short + ???
      return buffer.readShort(16);
    }

    logger.warn("Unsupported length {} of number block, assuming long type but truncating it to int", length - 2);
    return (int) buffer.readLong(8 * (length - 2));
  }

  @Override
  public BaseAnalogValue<?> getValue() {
    return value;
  }

}
