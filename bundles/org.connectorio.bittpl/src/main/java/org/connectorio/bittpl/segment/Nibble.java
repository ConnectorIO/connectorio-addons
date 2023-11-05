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
package org.connectorio.bittpl.segment;

import com.github.jinahya.bit.io.BitInput;
import com.github.jinahya.bit.io.BitOutput;
import java.io.IOException;

public class Nibble extends ElementBase {

  private final byte value;

  public Nibble(int offset, String nibble) {
    super(offset, 4);
    this.value = Byte.parseByte(nibble, 16);
  }

  @Override
  public boolean matches(BitInput input) throws IOException {
    int nibble = input.readByte(true, 4);
    return value == nibble;
  }

  @Override
  public void write(BitOutput output) throws IOException {
    output.writeByte(true, 4, value);
  }

  @Override
  public String toString() {
    return "Nibble [" + offset() + "=" + this.value + "]";
  }

}
