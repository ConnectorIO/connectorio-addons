/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.fatek.internal.discovery.broadcast;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses answers coming back from facon/Fatek PLC's and extracts information delivered by it.
 */
public class DiscoveryPacketParser {

  private static final byte[] HEADER = new byte[] {0x58, 0x76};

  private final Logger logger = LoggerFactory.getLogger(DiscoveryPacketParser.class);

  @SuppressWarnings({"ResultOfMethodCallIgnored"})
  public Optional<DiscoveryPacket> parse(byte[] payload) throws IOException {
    if (payload.length < 351) {
      logger.info("Ignoring packet, it is too short. Expected 351 bytes, found {}", payload.length);
      return Optional.empty();
    }

    //  * 11 bytes: junk
    // *   + 1  byte: header: 0a
    // *   + 2 bytes: flag 00 <rand>
    // *   + 2 bytes: flag 00 <rand>
    // *   + 4 bytes: junk (58 76 33 96)
    // *   + 1  byte: firmware (0x38 = 56/10 -> 5.6)
    // *   + 1  byte: junk (32)
    // *  6 bytes: MAC address
    // *  4 bytes: IP
    // *  4 bytes: Gateway
    // *  4 bytes: subnet
    // * 12 bytes: PLC Name
    // * 22 bytes: Description
    // * 12 bytes: junk
    // *  1  byte: 1e
    // *  2 bytes: facom port  (0x01F4 = 500)
    // *  1  byte: 15
    // *  2 bytes: modbus port (0x01F6 = 502)
    DataInputStream stream = new DataInputStream(new ByteArrayInputStream(payload));
    byte headerByte = stream.readByte();
    if (headerByte != 0x0A) {
      logger.info("Ignoring packet, packet header {} do not match template 0x0A", HexUtils.bytesToHex(new byte[] {headerByte}));
      return Optional.empty();
    }
    stream.skipBytes(4);
    byte[] header = new byte[2];
    stream.read(header);
    if (!Arrays.equals(header, HEADER)) {
      logger.info("Ignoring packet, packet static part {} do not match template {}", HexUtils.bytesToHex(header), HexUtils.bytesToHex(HEADER));
      return Optional.empty();
    }
    // variable part of packet, not sure what it is
    stream.skipBytes(2);

    DiscoveryPacket packet = new DiscoveryPacket();
    packet.setFirmwareVersion((double) stream.readUnsignedByte() / 10);
    stream.skipBytes(1);

    byte[] mac = new byte[6];
    stream.read(mac);
    packet.setMacAddress(formatHexSequence(mac));

    byte[] ipOrMask = new byte[4];
    stream.read(ipOrMask);
    packet.setIp(formatIpSequence(ipOrMask));
    stream.read(ipOrMask);
    packet.setGateway(formatIpSequence(ipOrMask));
    stream.read(ipOrMask);
    packet.setSubnet(formatIpSequence(ipOrMask));

    byte[] text = new byte[12];
    stream.read(text);
    packet.setPlcName(new String(text).trim());
    text = new byte[22];
    stream.read(text);
    packet.setPlcDescription(new String(text).trim());
    stream.skipBytes(13);
    packet.setPrimaryFaconPort(stream.readShort());
    stream.skipBytes(1);
    packet.setPrimaryModbusPort(stream.readShort());

    return Optional.of(packet);
  }

  private static String formatHexSequence(byte[] data) {
    String output = "";
    for (byte octet : data) {
      output += (output.length() > 0 ? ":" : "") + new String(HexUtils.byteToHex(octet));
    }
    return output;
  }

  private static String formatIpSequence(byte[] data) {
    String output = "";
    for (byte octet : data) {
      output += (output.length() > 0 ? "." : "") + (octet & 0xFF);
    }
    return output;
  }

}
