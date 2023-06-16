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
package org.connectorio.addons.binding.fatek.jfatek;

public enum Points {
  _10 (10, (byte) 0x00),
  _14 (14, (byte) 0x01),
  _20 (20, (byte) 0x02),
  _24 (24, (byte) 0x03),
  _32 (32, (byte) 0x04),
  _40 (40, (byte) 0x05),
  _60 (60, (byte) 0x06),
  UNKNOWN (0, (byte) 0xFF);

  private final int count;
  private final byte code;

  Points(int count, byte code) {
    this.count = count;
    this.code = code;
  }

  public int getCount() {
    return count;
  }

  @Override
  public String toString() {
    return super.toString();
  }

  static Points valueOf(byte code) {
      for (Points type : values()) {
        if (type.code == code) {
          return type;
        }
      }

      return UNKNOWN;
    }
  }