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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import org.connectorio.addons.transmitter.Requester;
import org.connectorio.addons.transmitter.RequesterCallback;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpRequester implements Requester<InetSocketAddress> {

  private final Logger logger = LoggerFactory.getLogger(UdpRequester.class);

  private final AtomicBoolean started = new AtomicBoolean();
  private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>(10);
  private final BlockingQueue<Message> receiveQueue = new LinkedBlockingQueue<>(100); // big enough for any shop floor I hope

  private final ScheduledExecutorService executor;

  private final InetSocketAddress broadcastAddress;
  private final DatagramSocket socket;
  private final Thread receiver;
  private final AtomicReference<RequesterCallback<InetSocketAddress>> callback = new AtomicReference<>();

  public UdpRequester(InetSocketAddress broadcastAddress, InetSocketAddress receiveAddress) throws SocketException {
    this.broadcastAddress = broadcastAddress;
    socket = new DatagramSocket(receiveAddress);

    executor = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "udp-requester-" + broadcastAddress));

    executor.scheduleAtFixedRate(new ResponseProcessor(receiveQueue, callback), 0, 1, TimeUnit.SECONDS);
    executor.scheduleAtFixedRate(new SendingRunnable(socket, sendQueue), 0, 5, TimeUnit.SECONDS);
    receiver = new Thread(new ReceivingRunnable(socket, started, receiveQueue), "udp-requester-" + broadcastAddress + "receiver");
  }

  @Override
  public void setCallback(RequesterCallback<InetSocketAddress> callback) {
    this.callback.set(callback);
  }

  @Override
  public CompletableFuture<Void> request(byte[] payload) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    sendQueue.offer(new Message(future, broadcastAddress, payload));
    return future;
  }

  public void start() {
    started.compareAndSet(false, true);

    // make sure we launch receiver, so we do not miss early answers for any broadcasts
    receiver.start();
  }

  @Override
  public void close() throws IOException {
    started.set(false);
    executor.shutdownNow();
    socket.close();
  }

  /*
  public static void main(String[] args) throws Exception {
    UdpRequester requester = new UdpRequester(
      new InetSocketAddress("255.255.255.255", 111),
      new InetSocketAddress("10.10.10.173", 53238)
    );
    requester.setCallback(new RequesterCallback<InetSocketAddress>() {
      @Override
      public void requestAnswered(InetSocketAddress address, byte[] answer) {
        System.out.println("Received answer " + address + " " + HexUtils.bytesToHex(answer));
      }
    });
    BiConsumer<Void, Throwable> consumer = (r, e) -> {
      if (e != null) {
        e.printStackTrace();
        return;
      }
      System.out.println("Packet sent");
    };
    requester.start();
    requester.request(HexUtils.hexToBytes("130c2f9b777089aa")).whenComplete(consumer);
    requester.request(HexUtils.hexToBytes("095083045a220022")).whenComplete(consumer);
  }
  */

}
