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
package org.connectorio.addons.binding.canopen.ta.tapi.dev.publisher;

import java.util.Map;
import org.apache.plc4x.java.spi.generation.ByteOrder;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.SerializationException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.generation.WriteBufferByteBased;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.ta.tapi.io.AnalogGroup;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.canopen.ta.tapi.val.AnalogValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalogInputRunnable extends PublishingRunnable {

  private final Logger logger = LoggerFactory.getLogger(AnalogInputRunnable.class);
  private final Map<Integer, TAAnalogInput> analogInput;
  private final int clientId;

  public AnalogInputRunnable(CoNode node, Map<Integer, TAAnalogInput> analogInput, int clientId) {
    super(node);
    this.analogInput = analogInput;
    this.clientId = clientId;
  }

  @Override
  public void run() {
    logger.debug("Publishing state of {} analog inputs.", analogInput.size());
    for (int index = 1; index <= 32; index += 4) {
      if (hasKeyInRange(analogInput, index, 4)) {
        AnalogGroup group = new AnalogGroup(clientId, index);
        logger.debug("Sending automated update of analog group {}", group);
        sendAnalog(group);
      }
    }
  }

  public void sendAnalog(AnalogGroup group) {
    int writeIndex = 0;
    try {
      WriteBufferByteBased buffer = new WriteBufferByteBased(8, ByteOrder.LITTLE_ENDIAN);
      logger.debug("Sending update of analog inputs from {} to {}", group.getStartBoundary(), group.getEndBoundary());

      for (int objectIndex = group.getStartBoundary(); objectIndex <= group.getEndBoundary(); objectIndex++) {
        TAAnalogInput input = analogInput.get(objectIndex);
        if (input == null) {
          logger.debug("Uninitialized input {}. assuming empty value", objectIndex);
          buffer.writeInt(16, 0);
          continue;
        }

        AnalogValue value = input.getValue();
        if (value == null) {
          buffer.writeInt(16, 0);
        } else {
          short encode = value.encode();
          logger.trace("Writing encoded value {} (0x{}) for {}", encode, Integer.toHexString(encode), value);
          buffer.writeShort(16, encode);
        }
        writeIndex++;
      }

      send(group.getNodeId(), group.getService(), buffer);
    } catch (SerializationException e) {
      logger.error("Failed to update analog group {}, failure at index {}", group, writeIndex, e);
    } catch (Exception e) {
      logger.error("An unexpected error while update of group {}, failure at index {}", group, writeIndex, e);
    }
  }

}
