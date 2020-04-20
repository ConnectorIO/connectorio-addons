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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.plc4x.java.ads.api.generic.types.AmsNetId;
import org.eclipse.smarthome.core.util.HexUtils;

public class UdpRouteRequest extends UdpStructure {

  private final String sourceIp;
  private final String sourceAms;
  private final String routeName;
  private final String username;
  private final String password;

  public UdpRouteRequest(String sourceIp, String username, String password) {
    this(sourceIp, sourceIp + ".1.1", sourceIp, username, password);
  }

  public UdpRouteRequest(String sourceIp, String sourceAms, String routeName, String username, String password) {
    this.sourceIp = sourceIp;
    this.sourceAms = sourceAms;
    this.routeName = routeName;
    this.username = username;
    this.password = password;
  }

  void send(String host) throws IOException {
    send(host, 48899);
  }

  void send(String host, int port) throws IOException {
    byte[] bytes = construct();

    InetAddress address = InetAddress.getByName(host);
    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 48899);

    DatagramSocket socket = new DatagramSocket();
    socket.send(packet);

    byte[] response = new byte[32];
    DatagramPacket udpPacket = new DatagramPacket(response, response.length);
    socket.receive(udpPacket);

    System.out.println(HexUtils.bytesToHex(udpPacket.getData()));

    System.out.println("Request " + this);
    System.out.println("Source packet " + HexUtils.bytesToHex(bytes));
  }

  public byte[] construct() {
    short sourceIpLen = Integer.valueOf(sourceIp.length() + 1).shortValue();
    short usernameLen = Integer.valueOf(username.length() + 1).shortValue();
    short passwordLen = Integer.valueOf(password.length() + 1).shortValue();
    short routeNameLen = Integer.valueOf(routeName.length() + 1).shortValue();

    int capacity = 50 + sourceIpLen + usernameLen + passwordLen + routeNameLen;
    ByteBuffer buffer = ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);

    AmsNetId senderAms = AmsNetId.of(sourceAms);

    // header
    buffer.put(HEADER);
    buffer.put(STATIC);
    buffer.put(ROUTE_REQUEST_TYPE);

    buffer.put(senderAms.getBytes()); // source ams
    buffer.putShort((short) 10000); // (source?) port
    buffer.putShort((short) 5); // command
    buffer.put(new byte[] {0x00, 0x00, 0x0C, 0x00}); // unknown
    buffer.putShort(sourceIpLen);
    buffer.put(sourceIp.getBytes(StandardCharsets.UTF_8));
    buffer.put((byte) 0x00); // string termination
    buffer.put(new byte[] {0x07, 0x00}); // unknown
    buffer.putShort((short) 6);
    buffer.put(senderAms.getBytes());

    writeString(buffer, new byte[] {0x0D, 0x00}, usernameLen, username);
    writeString(buffer, new byte[] {0x02, 0x00}, passwordLen, password);
    writeString(buffer, new byte[] {0x05, 0x00}, routeNameLen, routeName);

    byte[] packet = new byte[capacity];
    buffer.position(0);
    buffer.order(ByteOrder.BIG_ENDIAN).get(packet);
    return packet;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("UdpRouteRequest{");
    sb.append("sourceIp='").append(sourceIp).append('\'');
    sb.append(", sourceAms='").append(sourceAms).append('\'');
    sb.append(", routeName='").append(routeName).append('\'');
    sb.append(", username='").append(username).append('\'');
    sb.append(", password='").append(password).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UdpRouteRequest)) {
      return false;
    }
    UdpRouteRequest that = (UdpRouteRequest) o;
    return Objects.equals(sourceIp, that.sourceIp) &&
      Objects.equals(sourceAms, that.sourceAms) &&
      Objects.equals(routeName, that.routeName) &&
      Objects.equals(username, that.username) &&
      Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceIp, sourceAms, username, password);
  }

  public static UdpRouteRequest parse(byte[] bytes) {
    out("Outgoing packet", bytes);

    ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

    slice(buffer, 12); // header

    byte[] slice = slice(buffer, 6);
    AmsNetId amsSender = AmsNetId.of(slice);
    short amsPort = buffer.getShort();
    short command = buffer.getShort();
    slice(buffer, 4);  // unknown

    int hostnameLength = buffer.getShort();
    String hostname = new String(slice(buffer, hostnameLength), StandardCharsets.UTF_8);

    slice(buffer, 2); // unknown
    short amsLen = buffer.getShort();
    AmsNetId ams = AmsNetId.of(slice(buffer, amsLen));

    slice(buffer, 2); // unknown

    String username = readString(buffer);

    slice(buffer, 2); // unknown
    String password = readString(buffer);

    slice(buffer, 2); // unknown
    String routeName = readString(buffer);

    return new UdpRouteRequest(
      hostname, amsSender.toString(), routeName, username, password
    );
  }

}
