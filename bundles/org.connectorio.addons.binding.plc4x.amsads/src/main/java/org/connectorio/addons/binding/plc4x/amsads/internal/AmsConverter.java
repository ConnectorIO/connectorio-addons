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
package org.connectorio.addons.binding.plc4x.amsads.internal;

/**
 * Converter to swap AmsNetId to/from different forms.
 */
public class AmsConverter {

  public static org.apache.plc4x.java.ads.readwrite.AmsNetId createAms(String amsNetId) {
    final String[] parts = amsNetId.split("\\.");
    if (parts.length != 6) {
      throw new IllegalArgumentException("Invalid AmsNetId: " + amsNetId + ". Ams must have 6 segments!");
    }
    return new org.apache.plc4x.java.ads.readwrite.AmsNetId(
      Short.parseShort(parts[0]),
      Short.parseShort(parts[1]),
      Short.parseShort(parts[2]),
      Short.parseShort(parts[3]),
      Short.parseShort(parts[4]),
      Short.parseShort(parts[5])
    );
  }

  public static org.apache.plc4x.java.ads.discovery.readwrite.AmsNetId createDiscoveryAms(String amsNetId) {
    final String[] parts = amsNetId.split("\\.");
    if (parts.length != 6) {
      throw new IllegalArgumentException("Invalid AmsNetId: " + amsNetId + ". Ams must have 6 segments!");
    }
    return new org.apache.plc4x.java.ads.discovery.readwrite.AmsNetId(
      Short.parseShort(parts[0]),
      Short.parseShort(parts[1]),
      Short.parseShort(parts[2]),
      Short.parseShort(parts[3]),
      Short.parseShort(parts[4]),
      Short.parseShort(parts[5])
    );
  }

  public static String parseDiscoveryAms(org.apache.plc4x.java.ads.discovery.readwrite.AmsNetId amsNetId) {
    return amsNetId.getOctet1() + "." +
      amsNetId.getOctet2() + "." +
      amsNetId.getOctet3() + "." +
      amsNetId.getOctet4() + "." +
      amsNetId.getOctet5() + "." +
      amsNetId.getOctet6();
  }

}
