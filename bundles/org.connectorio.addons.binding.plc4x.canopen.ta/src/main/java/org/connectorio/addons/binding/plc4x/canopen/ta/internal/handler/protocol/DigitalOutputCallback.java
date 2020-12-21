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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.protocol;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.ValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalOutputCallback extends AbstractCallback {

  private final Logger logger = LoggerFactory.getLogger(DigitalOutputCallback.class);
  private final ValueListener listener;

  public DigitalOutputCallback(ValueListener listener) {
    this.listener = listener;
  }

  @Override
  public void accept(PlcSubscriptionEvent event) {
    byte[] data = getBytes(event, event.getFieldNames().iterator().next());
    ReadBuffer buffer = new ReadBuffer(data, true);
    try {
      for (int index = 0; index < 32; index++) {
        boolean status = buffer.readBit();
        logger.info("Digital Output {}={}", index + 1, status);
        listener.digital(index + 1, status);
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

}
