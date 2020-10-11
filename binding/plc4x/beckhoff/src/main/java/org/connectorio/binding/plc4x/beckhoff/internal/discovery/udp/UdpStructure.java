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
import java.nio.charset.StandardCharsets;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UdpStructure {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpStructure.class);
  protected static final byte[] HEADER = {0x03, 0x66, 0x14, 0x71};
  protected static final byte[] STATIC = {0x00, 0x00, 0x00, 0x00};

  protected static final byte[] ROUTE_REQUEST_TYPE = {0x06, 0x00, 0x00, 0x00};
  protected static final byte[] ROUTE_REPLY_TYPE = {0x06, 0x00, 0x00, (byte) 0x80};
  protected static final byte[] BROADCAST_REQUEST_TYPE = {0x01, 0x00, 0x00, 0x00};
  protected static final byte[] BROADCAST_REPLY_TYPE = {0x01, 0x00, 0x00, (byte) 0x80};

  public abstract byte[] construct();

  public static String readString(ByteBuffer buffer) {
    short length = (short) (buffer.getShort() - 1);
    return new String(slice(buffer, length), StandardCharsets.UTF_8);
  }

  protected void writeString(ByteBuffer buffer, byte[] unknown, short length, String value) {
    buffer.put(unknown);
    buffer.putShort(length);
    buffer.put(value.getBytes(StandardCharsets.UTF_8));
    buffer.put((byte) 0x00); // terminator
  }

  public static void in(String msg, byte[] array) {
    bytes(true, msg, array);
  }

  public static void out(String msg, byte[] array) {
    bytes(false, msg, array);
  }

  public static void bytes(boolean incoming, String msg, byte[] array) {
    if (LOGGER.isDebugEnabled()) {
      String output = HexUtils.bytesToHex(array);
      String hex = output.replace("(\\w{2})", "$1 ");
      LOGGER.debug("{} {}: {}", incoming ? "<-- " : "--->", msg, hex);
    }
  }

  public static byte[] slice(ByteBuffer buffer, int length) {
    byte[] slice = new byte[length];
    buffer.get(slice);
    return slice;
  }

  public static byte[] slice(byte[] buffer, int start, int length) {
    if (buffer.length < start + length) {
      return new byte[0];
    }

    byte[] slice = new byte[length];
    System.arraycopy(buffer, start, slice, 0, length);
    return slice;
  }

}
