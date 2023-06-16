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
package org.connectorio.addons.network.transmitter.ip.internal;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.connectorio.addons.transmitter.RequesterCallback;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResponseProcessor implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(ResponseProcessor.class);
  private final BlockingQueue<Message> queue;
  private final AtomicReference<RequesterCallback<InetSocketAddress>> callback;

  public ResponseProcessor(BlockingQueue<Message> queue, AtomicReference<RequesterCallback<InetSocketAddress>> callback) {
    this.queue = queue;
    this.callback = callback;
  }

  @Override
  public void run() {
    try {
      Message answer = queue.poll(1000, TimeUnit.MILLISECONDS);
      if (answer == null) {
        return;
      }

      logger.debug("Received response from {}, packet {}", answer.getAddress(), HexUtils.bytesToHex(answer.getPayload()));
      RequesterCallback<InetSocketAddress> requesterCallback = callback.get();
      if (requesterCallback != null) {
        requesterCallback.requestAnswered(answer.getAddress(), answer.getPayload());
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}