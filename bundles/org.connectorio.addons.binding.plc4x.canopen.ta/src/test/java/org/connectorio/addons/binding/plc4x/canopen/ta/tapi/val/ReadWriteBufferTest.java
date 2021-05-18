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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.junit.jupiter.api.Test;

public class ReadWriteBufferTest {

  @Test
  void testReadWrite() throws Exception {
    //new Argument(0x46ea, 3, 23.4),
    RASValue value = new RASValue((short) 0x46ea, AnalogUnit.TEMPERATURE_REGULATOR);

    WriteBuffer writeBuffer = new WriteBuffer(8, true);
    for (int index = 0; index < 4; index++) {
      writeBuffer.writeShort(16, value.encode());
    }

    byte[] data = writeBuffer.getData();
    ReadBuffer readBuffer = new ReadBuffer(data, true);
    for (int index = 0; index < 4; index++) {
      RASValue read = new RASValue(readBuffer.readShort(16), AnalogUnit.TEMPERATURE_REGULATOR);
      assertThat(read).isEqualTo(value);
    }
  }

  @Test
  void testReadCelsius33() throws Exception {
    byte[] data = {74, 1};
    AnalogValue value = new AnalogValue((short) 330, AnalogUnit.CELSIUS);
    ReadBuffer readBuffer = new ReadBuffer(data, true);
    short raw = readBuffer.readShort(16);
    AnalogValue read = new AnalogValue(raw, AnalogUnit.CELSIUS);
    assertThat(read).isEqualTo(value);
  }

  @Test
  void testReadCelsius100() throws Exception {
    byte[] data = {-24, 3};
    AnalogValue value = new AnalogValue((short) 1000, AnalogUnit.CELSIUS);
    ReadBuffer readBuffer = new ReadBuffer(data, true);
    short raw = readBuffer.readShort(16);
    AnalogValue read = new AnalogValue(raw, AnalogUnit.CELSIUS);
    assertThat(read).isEqualTo(value);
  }

}
