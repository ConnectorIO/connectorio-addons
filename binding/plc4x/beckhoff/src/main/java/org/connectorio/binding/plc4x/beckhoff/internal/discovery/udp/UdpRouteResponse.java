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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.apache.plc4x.java.ads.api.generic.types.AmsNetId;
import org.openhab.core.util.HexUtils;

public class UdpRouteResponse extends UdpStructure {

  private static final int CAPACITY = 32;
  private static byte[] SUCCESS = new byte[] { 0x04, 0x00, 0x00 };
  private static byte[] FAILURE = new byte[] { 0x00, 0x04, 0x07 };

  private String senderAms;
  private boolean success;

  public UdpRouteResponse(byte[] answer) {
    this(ByteBuffer.wrap(answer));
  }

  public UdpRouteResponse(ByteBuffer buffer) {
    parse(buffer.order(ByteOrder.LITTLE_ENDIAN));
  }

  private void parse(ByteBuffer buffer) {
    if (buffer.capacity() < CAPACITY) {
      throw new DiscoveryException("Unexpected answer received");
    }

    in("Header", slice(buffer, 12));
    byte[] senderAms = slice(buffer, 6);

    this.senderAms = AmsNetId.of(senderAms).toString();
    slice(buffer, 8); // unknown

    byte[] status = slice(buffer, 3);
    if (Arrays.equals(SUCCESS, status)) {
      this.success = true;
    } else if (Arrays.equals(FAILURE, status)) {
      this.success = false;
    } else {
      throw new DiscoveryException("Unknown status " + HexUtils.bytesToHex(status) + " received");
    }
  }

  public String getAmsNetId() {
    return this.senderAms;
  }

  public boolean isSuccess() {
    return success;
  }

  public byte[] construct() {
    ByteBuffer buffer = ByteBuffer.allocate(CAPACITY).order(ByteOrder.LITTLE_ENDIAN);
    buffer.put(HEADER);
    buffer.put(STATIC);
    buffer.put(ROUTE_REPLY_TYPE);
    buffer.put(AmsNetId.of(senderAms).getBytes());

    // constant part
    buffer.put(new byte[] {0x10, 0x27});
    buffer.put(new byte[] {0x01, 0x00, 0x00, 0x00, 0x01, 0x00});

    // status
    buffer.put(success ? SUCCESS : FAILURE);

    // end
    buffer.put(new byte[] {0x00, 0x00});

    return buffer.array();
  }
}
