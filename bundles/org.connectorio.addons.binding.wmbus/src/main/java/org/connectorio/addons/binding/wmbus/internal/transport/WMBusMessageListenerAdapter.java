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
package org.connectorio.addons.binding.wmbus.internal.transport;

import java.io.IOException;
import java.util.function.Consumer;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageDispatcher;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WMBusMessageListenerAdapter implements WMBusListener {

  private final Logger logger = LoggerFactory.getLogger(WMBusMessageListenerAdapter.class);
  private final WMBusMessageDispatcher dispatcher;
  private final Consumer<String> offlineCallback;

  public WMBusMessageListenerAdapter(WMBusMessageDispatcher dispatcher, Consumer<String> offlineCallback) {
    this.dispatcher = dispatcher;
    this.offlineCallback = offlineCallback;
  }

  @Override
  public void newMessage(WMBusMessage message) {
    logger.debug("Received message {}", message);
    dispatcher.dispatch(message);
  }

  @Override
  public void discardedBytes(byte[] bytes) {
    logger.debug("Discarding chunk of data stream containing {} bytes", bytes.length);
  }

  @Override
  public void stoppedListening(IOException cause) {
    logger.error("Error while reading serial data stream", cause);

    offlineCallback.accept("Error in receiver thread " + cause.getMessage());
  }

}