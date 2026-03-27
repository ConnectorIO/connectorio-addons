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
package org.connectorio.addons.network.can.dbus.internal.nm;

import java.util.Map;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

public class InterfaceFlags {

  private static final String INTERFACE_FLAGS_PROPERTY = "InterfaceFlags";

  private final static long FLAG_CARRIER = 0x10000;
  private final static long FLAG_UP = 0x01;
  private final static long FLAG_LOWER_UP = 0x02;

  private final long interfaceFlags;
  private final boolean up;

  public InterfaceFlags(long interfaceFlags) {
    this.interfaceFlags = interfaceFlags;
    this.up = (interfaceFlags & FLAG_UP) == FLAG_UP
      && (interfaceFlags & FLAG_LOWER_UP) == FLAG_LOWER_UP
      && (interfaceFlags & FLAG_CARRIER) == FLAG_CARRIER;
  }

  public boolean isUp() {
    return up;
  }

  @Override
  public String toString() {
    return "Interface Flags[" + Long.toHexString(interfaceFlags) + ", up=" + up + "]";
  }

  public static InterfaceFlags from(Map<String, Variant<?>> properties) {
    Variant<?> variant = properties.get(INTERFACE_FLAGS_PROPERTY);
    if (variant == null || !(variant.getValue() instanceof UInt32)) {
      return null;
    }

    long interfaceFlags = ((UInt32) variant.getValue()).longValue();
    return new InterfaceFlags(interfaceFlags);
  }

}