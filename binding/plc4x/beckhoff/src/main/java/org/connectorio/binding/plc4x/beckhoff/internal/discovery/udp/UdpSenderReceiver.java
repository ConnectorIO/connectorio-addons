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
package org.connectorio.binding.plc4x.beckhoff.internal.discovery.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.BeckhoffDiscoveryListener;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.BeckhoffRouteListener;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.DiscoveryReceiver;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.DiscoverySender;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.RouteReceiver;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = {
  DiscoverySender.class, DiscoveryReceiver.class, RouteReceiver.class
})
public class UdpSenderReceiver implements DiscoverySender, DiscoveryReceiver, RouteReceiver {

  private static final int BECKHOFF_UDP_PORT = 48899;

  private final Logger logger = LoggerFactory.getLogger(UdpSenderReceiver.class);

  private final AtomicBoolean started = new AtomicBoolean();
  private final BlockingQueue<Envelope> sendQueue = new LinkedBlockingQueue<>(10);
  private final BlockingQueue<Envelope> receiveQueue = new LinkedBlockingQueue<>(10000); // big enough for any shop floor I hope
  private final Set<BeckhoffDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();
  private final Set<BeckhoffRouteListener> routeListeners = new CopyOnWriteArraySet<>();
  private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("beckhoff");

  private final DatagramSocket socket;
  private final Thread sender;
  private final Thread receiver;

  public UdpSenderReceiver() throws SocketException {
    socket = new DatagramSocket(BECKHOFF_UDP_PORT);

    executor.scheduleAtFixedRate(new ResponseProcessor(started, receiveQueue, discoveryListeners, routeListeners), 0, 1, TimeUnit.SECONDS);
    sender = new Thread(new SendingRunnable(socket, started, sendQueue), "beckhoff-udp-sender");
    receiver = new Thread(new ReceivingRunnable(socket, started, receiveQueue), "beckhoff-udp-receiver");
  }

  @Override
  public void send(Envelope packet) {
    try {
      this.sendQueue.offer(packet, 5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.info("Could not send UDP packet", e);
    }
  }

  @Activate
  public void start() throws SocketException {
    started.compareAndSet(false, true);

    // make sure we launch receiver so we do not miss early answers for any broadcasts
    receiver.start();
    sender.start();
  }

  @Deactivate
  public void stop() {
    started.compareAndSet(true, false);

    socket.close();
  }

  @Override
  public void addDiscoveryListener(BeckhoffDiscoveryListener listener) {
    this.discoveryListeners.add(listener);
  }

  @Override
  public void removeDiscoveryListener(BeckhoffDiscoveryListener listener) {
    this.discoveryListeners.remove(listener);
  }

  @Override
  public void addRouteListener(BeckhoffRouteListener listener) {
    routeListeners.add(listener);
  }

  @Override
  public void removeRouteListener(BeckhoffRouteListener listener) {
    routeListeners.remove(listener);
  }

  private static class SendingRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SendingRunnable.class);
    private final DatagramSocket socket;
    private final AtomicBoolean started;
    private final BlockingQueue<Envelope> queue;

    public SendingRunnable(DatagramSocket socket, AtomicBoolean started, BlockingQueue<Envelope> queue) {
      this.socket = socket;
      this.started = started;
      this.queue = queue;
    }

    @Override
    public void run() {
      while (started.get()) {
        try {
          Envelope send = queue.poll(500, TimeUnit.MILLISECONDS);
          if (send != null) {
            byte[] frame = send.structure.construct();
            DatagramPacket packet = new DatagramPacket(frame, frame.length, InetAddress.getByName(send.host), BECKHOFF_UDP_PORT);

            socket.send(packet);
          }
        } catch (IOException | InterruptedException e) {
          logger.info("Could not send packet", e);
        }
      }
    }
  }

  private static class ReceivingRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ReceivingRunnable.class);
    private final DatagramSocket socket;
    private final AtomicBoolean started;
    private final BlockingQueue<Envelope> queue;

    public ReceivingRunnable(DatagramSocket socket, AtomicBoolean started, BlockingQueue<Envelope> queue) {
      this.socket = socket;
      this.started = started;
      this.queue = queue;
    }

    @Override
    public void run() {
      while (started.get()) {
        try {
          byte[] buffer = new byte[512];
          DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
          socket.receive(packet); // blocking

          String address = packet.getAddress().getHostAddress();

          parse(packet.getData(), address)
            .map(struct -> new Envelope(address, struct))
            .ifPresent(queue::offer);
        } catch (IOException e) {
          logger.info("Could not receive or handle received packet", e);
        }
      }
    }

    private Optional<UdpStructure> parse(byte[] data, String sender) {
      if (data.length < 12) {
        return Optional.empty();
      }

      byte[] header = UdpStructure.slice(data, 0, 4);
      if (!Arrays.equals(UdpStructure.HEADER, header)) {
        logger.warn("Host {} sent to frame with unknown header {}. Content: {}", sender, HexUtils.bytesToHex(header), HexUtils.bytesToHex(data));
      }

      header = UdpStructure.slice(data, 4, 4);
      if (!Arrays.equals(UdpStructure.STATIC, header)) {
        logger.warn("Host {} sent to us unmapped frame type {}. Content: {}", sender, HexUtils.bytesToHex(header), HexUtils.bytesToHex(data));
        return Optional.empty();
      }

      // a real message type
      header = UdpStructure.slice(data, 8, 4);
      if (Arrays.equals(UdpStructure.BROADCAST_REPLY_TYPE, header)) {
        logger.debug("Received identification frame from {}. Content: {}", sender, HexUtils.bytesToHex(data));
        return Optional.of(UdpDiscoveryResponse.parse(data));
      } else if (Arrays.equals(UdpStructure.ROUTE_REPLY_TYPE, header)) {
        logger.debug("Received answer for route request from {}. Content: {}", sender, HexUtils.bytesToHex(data));
        return Optional.of(new UdpRouteResponse(data));
      }

      logger.warn("Host {} sent to us unidentified frame type {}. Content: {}", sender, HexUtils.bytesToHex(header), HexUtils.bytesToHex(data));

      return Optional.empty();
    }
  }

  private static class ResponseProcessor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ResponseProcessor.class);
    private final AtomicBoolean started;
    private final BlockingQueue<Envelope> queue;
    private final Set<BeckhoffDiscoveryListener> discoveryListeners;
    private final Set<BeckhoffRouteListener> routeListeners;

    public ResponseProcessor(AtomicBoolean started, BlockingQueue<Envelope> queue, Set<BeckhoffDiscoveryListener> discoveryListeners, Set<BeckhoffRouteListener> routeListeners) {
      this.started = started;
      this.queue = queue;
      this.discoveryListeners = discoveryListeners;
      this.routeListeners = routeListeners;
    }

    @Override
    public void run() {
      while (started.get()) {
        Envelope reply = queue.poll();

        if (reply != null) {
          if (reply.structure instanceof UdpDiscoveryResponse) {
            UdpDiscoveryResponse identification = (UdpDiscoveryResponse) reply.structure;
            discoveryListeners.forEach(listener -> listener.deviceDiscovered(
              reply.host, identification.getName(), identification.getSourceAms()
            ));
          } else if (reply.structure instanceof UdpRouteResponse) {
            UdpRouteResponse route = (UdpRouteResponse) reply.structure;
            routeListeners.forEach(listener -> listener.add(reply.host, route.getAmsNetId(), route.isSuccess()));
          } else {
            logger.warn("Unknown response from {}, packet {}", reply.host, reply.structure);
          }
        }
      }
    }
  }

}
