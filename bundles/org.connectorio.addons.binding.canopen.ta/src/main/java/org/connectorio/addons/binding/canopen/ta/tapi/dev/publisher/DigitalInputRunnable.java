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
import java.util.Map.Entry;
import org.apache.plc4x.java.canopen.readwrite.CANOpenService;
import org.apache.plc4x.java.spi.generation.ByteOrder;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.SerializationException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.generation.WriteBufferByteBased;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.ta.tapi.io.AnalogGroup;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TADigitalInput;
import org.connectorio.addons.binding.canopen.ta.tapi.val.AnalogValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalInputRunnable extends PublishingRunnable {

  private final Logger logger = LoggerFactory.getLogger(DigitalInputRunnable.class);
  private final Map<Integer, TADigitalInput> digitalInput;
  private final int clientId;

  public DigitalInputRunnable(CoNode node, Map<Integer, TADigitalInput> digitalInput, int clientId) {
    super(node);
    this.digitalInput = digitalInput;
    this.clientId = clientId;
  }

  @Override
  public void run() {
    int writeIndex = 0;
    try {
      logger.trace("Sending update of {} digital inputs.", digitalInput.size());
      WriteBufferByteBased buffer = new WriteBufferByteBased(8, ByteOrder.LITTLE_ENDIAN);
      int sum = 0;
      for (Entry<Integer, TADigitalInput> entry : digitalInput.entrySet()) {
        int index = entry.getKey();
        TADigitalInput input = entry.getValue();
        if (input != null && input.getValue() != null) {
          sum += (input.getValue().getValue() ? 1 : 0) << (index - 1);
        }
        writeIndex++;
      }
      buffer.writeInt(32, sum);
      buffer.writeInt(32, 0);

      send(clientId, CANOpenService.TRANSMIT_PDO_1, buffer);
    } catch (SerializationException e) {
      logger.error("Failed to update digital inputs, failure at index {}", writeIndex, e);
    } catch (Exception e) {
      logger.error("An unexpected error while update of digital outputs, failure at index {}", writeIndex, e);
    }
  }

}
