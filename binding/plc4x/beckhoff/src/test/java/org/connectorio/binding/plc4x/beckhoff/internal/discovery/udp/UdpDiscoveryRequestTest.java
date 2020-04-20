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
 */
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

import org.connectorio.binding.plc4x.beckhoff.internal.discovery.udp.UdpDiscoveryRequest;
import org.junit.jupiter.api.Test;

class UdpDiscoveryRequestTest {

  private static byte[] OUTPUT = new byte[] {
    0x03, 0x66, 0x14, 0x71, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, (byte) 0x0A, (byte) 0x0A, (byte) 0x0A, (byte) 0x0A,
    0x01, 0x01, 0x10, 0x27, 0x00, 0x00, 0x00, 0x00
  };

  @Test
  void test() {
    UdpDiscoveryRequest request = new UdpDiscoveryRequest("10.10.10.10.1.1");

    assertThat(request.construct())
      .isEqualTo(OUTPUT);

    assertThat(UdpDiscoveryRequest.parse(request.construct()))
      .isEqualTo(request);
  }

}