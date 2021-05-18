/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.config;

public enum DeviceType {

  UVR16x2 (0x87),
  RSM610 (0x88),
  EZ3 (0x8F),
  UVR610(0x91),
  SIMULATOR (0x8A),

  UNKNOWN (0x00);

  private final int code;

  DeviceType(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }

  public static DeviceType fromCode(int code) {
    for (DeviceType type : values()) {
      if (type.code == code) {
        return type;
      }
    }

    return UNKNOWN;
  }

}
