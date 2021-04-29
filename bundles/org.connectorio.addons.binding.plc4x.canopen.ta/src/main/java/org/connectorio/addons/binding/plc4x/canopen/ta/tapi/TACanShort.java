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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi;

import java.util.concurrent.CompletableFuture;
import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.AnalogValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TACanShort extends TACanValue<Value<?>> {

  private final Logger logger = LoggerFactory.getLogger(TACanShort.class);

  private short unit;

  public TACanShort(CoNode node, int index, int subIndex) {
    this(node, (short) index, (short) subIndex);
  }

  public TACanShort(CoNode node, short index, short subIndex) {
    super(node, index, subIndex, CANOpenDataType.RECORD);
  }

  @Override
  protected CompletableFuture<Value<?>> initialize(CompletableFuture<byte[]> future) {
    CompletableFuture<Value<?>> answer = future.thenApply(this::map);
    return answer.whenComplete((response, error) -> {
      if (error == null) {
        this.value = response;
      } else {
        if (logger.isInfoEnabled()) {
          logger.debug("Failed to load short 0x{}/0x{}", Integer.toHexString(index), Integer.toHexString(subIndex));
        }
        if (logger.isTraceEnabled()) {
          logger.trace("Failed to load short 0x{}/0x{}", Integer.toHexString(index), Integer.toHexString(subIndex), error);
        }
      }
    });
  }

  private Value<?> map(byte[] array) {
    if (array[0] == (byte) 0x30 || array[0] == (byte) 0xb0) {
      short unit = array[1];
      short value = array[2];
      return new AnalogValue(value, AnalogUnit.valueOf(unit));
    } else {
      logger.info("Failed to read short value from ta, received value {}", Hex.encodeHexString(array));
      throw new IllegalArgumentException("Unsupported value type " + Hex.encodeHexString(array));
    }
  }

}
