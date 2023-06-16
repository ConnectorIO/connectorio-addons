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
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReceivingRunnable implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(ReceivingRunnable.class);
  private final DatagramSocket socket;
  private final AtomicBoolean started;
  private final BlockingQueue<Message> queue;

  public ReceivingRunnable(DatagramSocket socket, AtomicBoolean started, BlockingQueue<Message> queue) {
    this.socket = socket;
    this.started = started;
    this.queue = queue;
  }

  @Override
  public void run() {
    while (started.get() && !socket.isClosed()) {
      try {
        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
        socket.receive(packet);

        queue.put(
          new Message(null, new InetSocketAddress(packet.getAddress(), packet.getPort()), packet.getData())
        );
        Thread.sleep(1000);
      } catch (SocketException e) {
        logger.debug("Socket level exception, closing up listen loop.");
      } catch (IOException | InterruptedException e) {
        logger.info("Could not receive or handle received packet", e);
      }
    }
  }

}