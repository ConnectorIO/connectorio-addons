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

import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AnalogGroupTest {

  int NODE_ID = 10;

  @Test
  public void testAnalogs() {
    range(1, 4).mapToObj(this::getAddress).forEach(value -> write(value, 0x200, NODE_ID));
    range(5, 8).mapToObj(this::getAddress).forEach(value -> write(value, 0x280, NODE_ID));
    range(9, 12).mapToObj(this::getAddress).forEach(value -> write(value, 0x300, NODE_ID));
    range(13, 16).mapToObj(this::getAddress).forEach(value -> write(value, 0x380, NODE_ID));
    range(17, 20).mapToObj(this::getAddress).forEach(value -> write(value, 0x240, NODE_ID));
    range(21, 24).mapToObj(this::getAddress).forEach(value -> write(value, 0x2C0, NODE_ID));
    range(25, 28).mapToObj(this::getAddress).forEach(value -> write(value, 0x340, NODE_ID));
    range(29, 32).mapToObj(this::getAddress).forEach(value -> write(value, 0x3C0, NODE_ID));
  }

  private IntStream range(int start, int end) {
    return IntStream.range(start, end + 1);
  }

  private AnalogGroup getAddress(int address) {
    return new AnalogGroup(NODE_ID, address);
  }

  private void write(AnalogGroup value, int base, int nodeId) {
    Assertions.assertThat(value.getCobId())
      .isEqualTo(base + nodeId);
  }

}