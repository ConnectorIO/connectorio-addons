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
package org.connectorio.addons.binding.canopen.ta.tapi.dev;

import java.util.function.Consumer;
import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.spi.generation.ByteOrder;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.ReadBufferByteBased;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCallback implements Consumer<byte[]> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private final boolean littleEndian;

  protected AbstractCallback() {
    this(true);
  }

  protected AbstractCallback(boolean littleEndian) {
    this.littleEndian = littleEndian;
  }

  @Override
  public void accept(byte[] bytes) {
    try {
      accept(new ReadBufferByteBased(bytes, littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN) {
        @Override
        public String toString() {
          return "ReadBuffer [" + Hex.encodeHexString(bytes) + ", pos=" + getPos() + "]";
        }
      });
    } catch (ParseException e) {
      logger.warn("Failed to parse callback information", e);
    }
  }

  protected abstract void accept(ReadBuffer buffer) throws ParseException;

}
