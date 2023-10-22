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
package org.connectorio.addons.binding.amsads.internal.discovery.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscovery;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlock;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockHostName;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockStatus;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockType;
import org.apache.plc4x.java.ads.discovery.readwrite.AmsString;
import org.apache.plc4x.java.ads.discovery.readwrite.Operation;
import org.apache.plc4x.java.ads.discovery.readwrite.Status;
import org.apache.plc4x.java.spi.generation.ByteOrder;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.generation.ReadBufferByteBased;
import org.apache.plc4x.java.spi.generation.SerializationException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.generation.WriteBufferByteBased;
import org.connectorio.addons.binding.amsads.internal.AmsConverter;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsDiscoveryDriver;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsDiscoveryListener;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsRouteListener;
import org.connectorio.addons.binding.amsads.internal.discovery.DiscoveryReceiver;
import org.connectorio.addons.binding.amsads.internal.discovery.DiscoverySender;
import org.connectorio.addons.binding.amsads.internal.discovery.RouteReceiver;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = {
  DiscoverySender.class, DiscoveryReceiver.class, RouteReceiver.class, AmsAdsDiscoveryDriver.class
})
public class UdpSenderReceiver implements DiscoverySender, DiscoveryReceiver, RouteReceiver, AmsAdsDiscoveryDriver {

  private static final int AMSADS_UDP_PORT = 48899;

  private final Logger logger = LoggerFactory.getLogger(UdpSenderReceiver.class);

  private final AtomicBoolean started = new AtomicBoolean();
  private final BlockingQueue<Envelope> sendQueue = new LinkedBlockingQueue<>(10);
  private final BlockingQueue<Envelope> receiveQueue = new LinkedBlockingQueue<>(10000); // big enough for any shop floor I hope
  private final Set<AmsAdsDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();
  private final Set<AmsAdsRouteListener> routeListeners = new CopyOnWriteArraySet<>();
  private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("amsads");

  private final DatagramSocket socket;
  private final Thread sender;
  private final Thread receiver;

  public UdpSenderReceiver() throws SocketException {
    socket = new DatagramSocket(AMSADS_UDP_PORT);

    executor.scheduleAtFixedRate(new ResponseProcessor(started, receiveQueue, discoveryListeners, routeListeners), 0, 1, TimeUnit.SECONDS);
    sender = new Thread(new SendingRunnable(socket, started, sendQueue), "amsads-udp-sender");
    receiver = new Thread(new ReceivingRunnable(socket, started, receiveQueue), "amsads-udp-receiver");
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
  public void addDiscoveryListener(AmsAdsDiscoveryListener listener) {
    this.discoveryListeners.add(listener);
  }

  @Override
  public void removeDiscoveryListener(AmsAdsDiscoveryListener listener) {
    this.discoveryListeners.remove(listener);
  }

  @Override
  public void addRouteListener(AmsAdsRouteListener listener) {
    routeListeners.add(listener);
  }

  @Override
  public void removeRouteListener(AmsAdsRouteListener listener) {
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
            final AdsDiscovery structure = send.structure;
            final WriteBufferByteBased buffer = new WriteBufferByteBased(structure.getLengthInBytes(), ByteOrder.LITTLE_ENDIAN);
            structure.serialize(buffer);
            byte[] frame = buffer.getBytes();
            DatagramPacket packet = new DatagramPacket(frame, frame.length, InetAddress.getByName(send.host), AMSADS_UDP_PORT);

            socket.send(packet);
          }
          Thread.sleep(1000);
        } catch (SerializationException | IOException | InterruptedException e) {
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

    private Optional<AdsDiscovery> parse(byte[] data, String sender) {
      if (data.length < 12) {
        return Optional.empty();
      }

      try {
        final AdsDiscovery discovery = AdsDiscovery.staticParse(new ReadBufferByteBased(data, ByteOrder.LITTLE_ENDIAN));
        logger.debug("Received valid discovery frame from {}. Content: {}", sender, HexUtils.bytesToHex(data));
        return Optional.of(discovery);
      } catch (ParseException e) {
        logger.warn("Host {} sent to us unidentified frame type {}. Content: {}", sender, HexUtils.bytesToHex(data), HexUtils.bytesToHex(data), e);
      }
      return Optional.empty();
    }
  }

  private static class ResponseProcessor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ResponseProcessor.class);
    private final AtomicBoolean started;
    private final BlockingQueue<Envelope> queue;
    private final Set<AmsAdsDiscoveryListener> discoveryListeners;
    private final Set<AmsAdsRouteListener> routeListeners;

    public ResponseProcessor(AtomicBoolean started, BlockingQueue<Envelope> queue, Set<AmsAdsDiscoveryListener> discoveryListeners, Set<AmsAdsRouteListener> routeListeners) {
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
          if (reply.structure instanceof AdsDiscovery) {
            AdsDiscovery identification = (AdsDiscovery) reply.structure;
            if (identification.getOperation() == Operation.DISCOVERY_RESPONSE) {
              String hostname = identification.getBlocks().stream().filter(block -> block instanceof AdsDiscoveryBlockHostName)
                .map(AdsDiscoveryBlockHostName.class::cast)
                .map(AdsDiscoveryBlockHostName::getHostName)
                .map(AmsString::getText)
                .findFirst().orElse("<unknown>");
              discoveryListeners.forEach(listener -> listener.deviceDiscovered(
                reply.host, hostname, AmsConverter.parseDiscoveryAms(identification.getAmsNetId())
              ));
            }
            if (identification.getOperation() == Operation.ADD_OR_UPDATE_ROUTE_RESPONSE) {
              boolean success = false;
              for (AdsDiscoveryBlock discoveryBlock : identification.getBlocks()) {
                if (discoveryBlock instanceof AdsDiscoveryBlockStatus) {
                  AdsDiscoveryBlockStatus statusBlock = (AdsDiscoveryBlockStatus) discoveryBlock;
                  if (statusBlock.getStatus() != Status.SUCCESS) {
                    logger.warn("Route setup failed, operation status is {}", statusBlock.getStatus());
                  }
                  success = statusBlock.getStatus() == Status.SUCCESS;
                }
              }
              for (AmsAdsRouteListener listener : routeListeners) {
                listener.add(reply.host, AmsConverter.parseDiscoveryAms(identification.getAmsNetId()), success);
              }
            }
          } else {
            logger.warn("Unknown response from {}, packet {}", reply.host, reply.structure);
          }
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            logger.error("Thread was interrupted", e);
          }
        }
      }
    }
  }

}
