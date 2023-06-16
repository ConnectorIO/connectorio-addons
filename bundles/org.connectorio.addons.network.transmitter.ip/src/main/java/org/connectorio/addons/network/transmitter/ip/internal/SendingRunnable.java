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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SendingRunnable implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(SendingRunnable.class);
  private final DatagramSocket socket;
  private final BlockingQueue<Message> queue;

  public SendingRunnable(DatagramSocket socket, BlockingQueue<Message> queue) {
    this.socket = socket;
    this.queue = queue;
  }

  @Override
  public void run() {
    Message message = null;
    try {
      message = queue.poll(500, TimeUnit.MILLISECONDS);
      if (message != null && !socket.isClosed()) {
        final byte[] frame = message.getPayload();
        DatagramPacket packet = new DatagramPacket(frame, frame.length, message.getAddress());
        socket.send(packet);
        message.getCallback().complete(null);
      }
    } catch (IOException | InterruptedException e) {
      if (message != null) {
        logger.info("Could not send packet", e);
        message.getCallback().completeExceptionally(e);
      }
    }
  }
}