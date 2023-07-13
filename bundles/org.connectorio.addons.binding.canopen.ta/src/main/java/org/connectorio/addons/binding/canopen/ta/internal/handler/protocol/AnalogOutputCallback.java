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

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.addons.binding.canopen.ta.internal.handler.ValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalogOutputCallback extends AbstractCallback {

  private final Logger logger = LoggerFactory.getLogger(AnalogOutputCallback.class);
  private final ValueListener listener;
  private final int offset;

  public AnalogOutputCallback(ValueListener listener, int offset) {
    this.listener = listener;
    this.offset = offset;
  }

  @Override
  public void accept(PlcSubscriptionEvent event) {
    byte[] bytes = getBytes(event, event.getFieldNames().iterator().next());

    ReadBuffer buffer = new ReadBuffer(bytes, true);
    try {
      for (int index = 1; index < 5; index++) {
        // we could use here getBytes or just delegate reading to unit
        listener.analog(offset + index, buffer);
      }
    }catch (ParseException e) {
      e.printStackTrace();
    }
  }

}
