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

class UdpRouteRequestTest {

  public static final String SOURCE_IP = "10.10.10.10";
  public static final String SOURCE_AMS = "10.10.10.10.1.1";
  public static final String ROUTE_NAME = "10.10.10.10";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";

  public static byte[] EXPECTED_OUTPUT = new byte[] {
    0x03, 0x66, 0x14, 0x71, 0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x0a, 0x0a, 0x0a, 0x0a,
    0x01, 0x01, 0x10, 0x27, 0x05, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x0c, 0x00, 0x31, 0x30, 0x2e, 0x31,
    0x30, 0x2e, 0x31, 0x30, 0x2e, 0x31, 0x30, 0x00, 0x07, 0x00, 0x06, 0x00, 0x0a, 0x0a, 0x0a, 0x0a,
    0x01, 0x01, 0x0d, 0x00, 0x09, 0x00, 0x75, 0x73, 0x65, 0x72, 0x6e, 0x61, 0x6d, 0x65, 0x00, 0x02,
    0x00, 0x09, 0x00, 0x70, 0x61, 0x73, 0x73, 0x77, 0x6f, 0x72, 0x64, 0x00, 0x05, 0x00, 0x0c, 0x00,
    0x31, 0x30, 0x2e, 0x31, 0x30, 0x2e, 0x31, 0x30, 0x2e, 0x31, 0x30, 0x00
  };

  @Test
  void testRouteRequest() {
    UdpRouteRequest request = new UdpRouteRequest(
      SOURCE_IP,
      SOURCE_AMS,
      ROUTE_NAME,
      USERNAME,
      PASSWORD
    );

    byte[] bytes = request.construct();

    assertThat(bytes).as("Generated output doesn't match expected structure")
      .hasSize(92)
      .isEqualTo(EXPECTED_OUTPUT);
  }

}