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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.publisher;

import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.values.PlcSINT;
import org.apache.plc4x.java.spi.values.PlcValues;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PublishingRunnable implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final CoNode node;

  protected PublishingRunnable(CoNode node) {
    this.node = node;
  }

  protected final void send(int nodeId, CANOpenService service, WriteBuffer buffer) {
    byte[] data = buffer.getData();
    logger.trace("Send to node {} {} (cob {}) data: {}", nodeId, service, Integer.toHexString(service.getMin() + nodeId), Hex.encodeHexString(data));
    node.getConnection().send(nodeId, service, PlcValues.of(
      new PlcSINT(data[0]), new PlcSINT(data[1]), new PlcSINT(data[2]), new PlcSINT(data[3]),
      new PlcSINT(data[4]), new PlcSINT(data[5]), new PlcSINT(data[6]), new PlcSINT(data[7])
    ));
  }

  protected boolean hasKeyInRange(Map<Integer, ?> elements, int start, int limit) {
    for (int index = start, end = index + start; index < end; index++) {
      if (elements.containsKey(index)) {
        return true;
      }
    }
    return false;
  }
}
