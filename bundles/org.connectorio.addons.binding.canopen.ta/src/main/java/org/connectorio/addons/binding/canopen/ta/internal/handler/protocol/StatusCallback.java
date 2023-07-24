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
package org.connectorio.addons.binding.canopen.ta.internal.handler.protocol;

import java.util.function.Consumer;
import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.spi.generation.ByteOrder;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.ReadBufferByteBased;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusCallback extends AbstractCallback {

  private final Logger logger = LoggerFactory.getLogger(StatusCallback.class);
  private final Consumer<Boolean> connected;
  private final int nodeId;
  private final int clientId;

  public StatusCallback(Consumer<Boolean> connected, int nodeId, int clientId) {
    this.connected = connected;
    this.nodeId = nodeId;
    this.clientId = clientId;
  }

  @Override
  public void accept(PlcSubscriptionEvent event) {
    try {
      byte[] answer = getBytes(event, "status");

      ReadBuffer buffer = new ReadBufferByteBased(answer, ByteOrder.LITTLE_ENDIAN);        // 0
      int mpdoId = 0x80 | buffer.readUnsignedInt(8);
      int index = buffer.readUnsignedInt(16);                     // 1+2
      int subIndex = buffer.readUnsignedInt(8);                   // 3
      int answerClientId = 0x640 ^ buffer.readUnsignedInt(16);    // 4+5
      int flag = buffer.readUnsignedInt(8);                       // 6
      int status = buffer.readUnsignedInt(8);                     // 7

      logger.debug("Received status payload {}", Hex.encodeHexString(answer));
      // 89 80 12 01 49 06 00 00
      if (answerClientId == clientId) {
        if (status == 0x00) {
          logger.debug("Client successfully logged in");
          connected.accept(true);
        } else if (status == 0x80) {
          logger.debug("Client login failed");
          connected.accept(false);
        } else {
          logger.warn("Failed to handle login answer, unknown status");
        }
      } else {
        logger.debug("Received MPDO for other node {}", answerClientId);
      }
    } catch (Exception e) {
      logger.error("Could not parse status payload", e);
    }
  }
}
