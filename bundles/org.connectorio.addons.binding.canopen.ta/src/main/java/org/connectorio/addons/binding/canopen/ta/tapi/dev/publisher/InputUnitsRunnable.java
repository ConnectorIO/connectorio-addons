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
import org.apache.plc4x.java.canopen.readwrite.CANOpenService;
import org.apache.plc4x.java.spi.generation.ByteOrder;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.SerializationException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.generation.WriteBufferByteBased;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TADigitalInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputUnitsRunnable extends PublishingRunnable {

  private final Logger logger = LoggerFactory.getLogger(InputUnitsRunnable.class);
  private final Map<Integer, TAAnalogInput> analogInput;
  private final Map<Integer, TADigitalInput> digitalInput;
  private final int clientId;

  public InputUnitsRunnable(CoNode node, Map<Integer, TAAnalogInput> analogInput, Map<Integer, TADigitalInput> digitalInput, int clientId) {
    super(node);
    this.analogInput = analogInput;
    this.digitalInput = digitalInput;
    this.clientId = clientId;
  }

  @Override
  public void run() {
    logger.debug("Publishing information about units used in own outputs (inputs for other nodes).");
    try {
      for (int index = 1; index <= 32; index += 6) {
        if (!hasKeyInRange(analogInput, index, 6)) {
          continue;
        }

        WriteBufferByteBased buffer = new WriteBufferByteBased(8, ByteOrder.LITTLE_ENDIAN);
        buffer.writeUnsignedShort(8, (short) 1);  // first byte is static value
        buffer.writeUnsignedShort(8, (short) (index - 1)); // call count - 0x00..0x05 analog

        for (int offset = 0; offset < 6; offset++) {
          int objectIndex = index + offset;
          if (analogInput.containsKey(objectIndex)) {
            TAAnalogInput input = analogInput.get(objectIndex);
            logger.trace("Writing unit for CAN Input #{}, {} mapped unit {}", objectIndex, input, AnalogUnit.valueOf(input.getUnit()));
            buffer.writeUnsignedShort(8, (short) input.getUnit());
          } else {
            // unknown input/unit
            logger.trace("Uninitialized CAN Input #{}, analog assuming 0 as configured unit.", objectIndex);
            buffer.writeUnsignedShort(8, (short) 0);
          }
        }
        send(clientId + 0x40, CANOpenService.TRANSMIT_PDO_1, buffer);
      }

      for (int index = 1; index <= 32; index += 6) {
        if (!hasKeyInRange(digitalInput, index, 6)) {
          continue;
        }

        WriteBufferByteBased buffer = new WriteBufferByteBased(8, ByteOrder.LITTLE_ENDIAN);
        buffer.writeUnsignedShort(8, (short) 1);
        buffer.writeUnsignedShort(8, (short) (6 + index - 1));  // call count - 0x06..0x0B digital

        for (int offset = 0; offset < 6; offset++) {
          int objectIndex = index + offset;
          if (digitalInput.containsKey(objectIndex)) {
            TADigitalInput input = digitalInput.get(objectIndex);
            logger.trace("Writing unit for CAN Input {}, {} mapped unit {}", objectIndex, input, DigitalUnit.valueOf(input.getUnit()));
            buffer.writeUnsignedShort(8, (short) input.getUnit());
          } else {
            // unknown input/unit
            logger.trace("Uninitialized CAN Input #{}, digital assuming 0 as configured unit.", objectIndex);
            buffer.writeUnsignedShort(8, (short) 0);
          }
        }
        send(clientId + 0x40, CANOpenService.TRANSMIT_PDO_1, buffer);
      }
    } catch (SerializationException e) {
      logger.error("Failed to publish configured input units", e);
    } catch (Exception e) {
      logger.error("An unexpected error while publishing unit information", e);
    }
  }

}
