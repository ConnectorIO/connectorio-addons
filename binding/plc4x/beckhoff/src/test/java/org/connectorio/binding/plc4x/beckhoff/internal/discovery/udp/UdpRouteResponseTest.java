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
package org.connectorio.binding.plc4x.beckhoff.internal.discovery.udp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UdpRouteResponseTest {

  private static byte[] SUCCESS = new byte[] {
    0x03, 0x66, 0x14, 0x71, 0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, (byte) 0x80, (byte) 0xc0, (byte) 0xa8, 0x02, (byte) 0xdd,
    0x01, 0x01, 0x10, 0x27, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00,
  };

  private static byte[] FAILURE = new byte[] {
    0x03, 0x66, 0x14, 0x71, 0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, (byte) 0x80, (byte) 0xc0, (byte) 0xa8, (byte) 0x02, (byte) 0xdd,
    0x01, 0x01, 0x10, 0x27, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x04, 0x07, 0x00, 0x00, 0x00,
  };

  @Test
  void testAnswer() {
    UdpRouteResponse response = new UdpRouteResponse(SUCCESS);

    assertThat(response.getAmsNetId())
      .isEqualTo("192.168.2.221.1.1");

    assertThat(response.isSuccess())
      .isEqualTo(true);

    assertThat(response.construct())
      .isEqualTo(SUCCESS);
  }

  @Test
  void testFailedAnswer() {
    UdpRouteResponse response = new UdpRouteResponse(FAILURE);

    assertThat(response.getAmsNetId())
      .isEqualTo("192.168.2.221.1.1");

    assertThat(response.isSuccess())
      .isEqualTo(false);

    assertThat(response.construct())
      .isEqualTo(FAILURE);
  }
}