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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AnalogGroupTest {

  static int NODE_ID = 10;

  @MethodSource
  @ParameterizedTest
  void verifyGroup(Argument argument) {
    AnalogGroup group = new AnalogGroup(NODE_ID, argument.index);

    assertThat(group.getNodeId()).isEqualTo(argument.node);
    assertThat(group.getStartBoundary()).isEqualTo(argument.start);
    assertThat(group.getEndBoundary()).isEqualTo(argument.end);
  }

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

    range(1, 4).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.RECEIVE_PDO_1, NODE_ID));
    range(5, 8).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.TRANSMIT_PDO_2, NODE_ID));
    range(9, 12).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.RECEIVE_PDO_2, NODE_ID));
    range(13, 16).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.TRANSMIT_PDO_3, NODE_ID));
    range(17, 20).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.RECEIVE_PDO_1, 0x40 + NODE_ID));
    range(21, 24).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.TRANSMIT_PDO_2, 0x40 + NODE_ID));
    range(25, 28).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.RECEIVE_PDO_2, 0x40 + NODE_ID));
    range(29, 32).mapToObj(this::getAddress).forEach(value -> write(value, CANOpenService.TRANSMIT_PDO_3, 0x40 + NODE_ID));
  }

  private IntStream range(int start, int end) {
    return IntStream.rangeClosed(start, end);
  }

  private AnalogGroup getAddress(int address) {
    return new AnalogGroup(NODE_ID, address);
  }

  private void write(AnalogGroup value, int base, int nodeId) {
    assertThat(value.getCobId())
      .isEqualTo(base + nodeId);
  }

  private void write(AnalogGroup value, CANOpenService service, int nodeId) {
    assertThat(value.getCobId())
      .isEqualTo(service.getMin() + nodeId);
  }

  private static List<Argument> verifyGroup() {
    return Arrays.asList(
      new Argument(1, 1, 4, NODE_ID),
      new Argument(4, 1, 4, NODE_ID),
      new Argument(5, 5, 8, NODE_ID),
      new Argument(8, 5, 8, NODE_ID),
      new Argument(9, 9, 12, NODE_ID),
      new Argument(12, 9, 12, NODE_ID),
      new Argument(13, 13, 16, NODE_ID),
      new Argument(16, 13, 16, NODE_ID),
      new Argument(17, 17, 20, 0x40 + NODE_ID),
      new Argument(20, 17, 20, 0x40 + NODE_ID),
      new Argument(21, 21, 24, 0x40 + NODE_ID),
      new Argument(24, 21, 24, 0x40 + NODE_ID),
      new Argument(25, 25, 28, 0x40 + NODE_ID),
      new Argument(28, 25, 28, 0x40 + NODE_ID),
      new Argument(29, 29, 32, 0x40 + NODE_ID),
      new Argument(32, 29, 32, 0x40 + NODE_ID)
    );
  }

  static class Argument {
    final int index;
    final int start;
    final int end;
    final int node;

    Argument(int index, int start, int end, int node) {
      this.index = index;
      this.start = start;
      this.end = end;
      this.node = node;
    }

    @Override
    public String toString() {
      return "node " + node + ", index " + index + ", group start=" + start + ", end=" + end;
    }
  }

}