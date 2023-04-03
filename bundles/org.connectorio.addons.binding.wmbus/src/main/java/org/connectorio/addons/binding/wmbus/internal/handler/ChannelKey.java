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
package org.connectorio.addons.binding.wmbus.internal.handler;

import java.util.Arrays;
import org.openhab.core.util.HexUtils;

public class ChannelKey {

  private final byte[] dib;
  private final byte[] vib;

  public ChannelKey(String dib, String vib) {
    this(HexUtils.hexToBytes(dib), HexUtils.hexToBytes(vib));
  }

  public ChannelKey(byte[] dib, byte[] vib) {
    this.dib = dib;
    this.vib = vib;
  }

  public String asString() {
    return HexUtils.bytesToHex(dib) + "_" + HexUtils.bytesToHex(vib);
  }

  @Override
  public String toString() {
    return "ChannelKey[DIB: " + HexUtils.bytesToHex(dib) + ", VIB:" + HexUtils.bytesToHex(vib) + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChannelKey)) {
      return false;
    }
    ChannelKey that = (ChannelKey) o;
    return Arrays.equals(dib, that.dib) && Arrays.equals(vib, that.vib);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(dib);
    result = 31 * result + Arrays.hashCode(vib);
    return result;
  }

}
