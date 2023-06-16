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
package org.connectorio.addons.binding.fatek.internal.discovery;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.connectorio.addons.binding.fatek.FatekBindingConstants;
import org.connectorio.addons.binding.fatek.internal.discovery.broadcast.DiscoveryPacket;
import org.connectorio.addons.binding.fatek.internal.discovery.broadcast.DiscoveryPacketParser;
import org.connectorio.addons.binding.fatek.internal.transport.JFatekTcpFaconConnection;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.network.ip.IpNetwork;
import org.connectorio.addons.network.ip.IpNetworkInterfaceTypes;
import org.connectorio.addons.network.ip.IpNetworkTypes;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceRegistry;
import org.connectorio.addons.network.iface.NetworkInterfaceStateCallback;
import org.connectorio.addons.network.transmitter.ip.UdpRequesterConfiguration;
import org.connectorio.addons.transmitter.Requester;
import org.connectorio.addons.transmitter.RequesterCallback;
import org.connectorio.addons.transmitter.RequesterFactory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DiscoveryService.class)
public class FatekDiscoveryService extends AbstractFatekDiscoveryService implements
    NetworkInterfaceStateCallback, RequesterCallback<InetSocketAddress> {

  public static final int LISTEN_PORT = 53238;
  public static final int BROADCAST_PORT = 111;

  private final static byte[] DISCOVERY_PACKET_1 = new byte[] {0x13, 0x0C, 0x2F, (byte) 0x9B, 0x77, 0x70, (byte) 0x89, (byte) 0xAA};
  private final static byte[] DISCOVERY_PACKET_2 = new byte[] {0x09, 0x50, (byte) 0x83, 0x04, 0x5A, 0x22, 0x00, 0x22};

  private final Logger logger = LoggerFactory.getLogger(FatekDiscoveryService.class);
  private final Map<IpNetwork, Requester<InetSocketAddress>> networkDiscoverers = new ConcurrentHashMap<>();
  private final NetworkInterfaceRegistry networkInterfaceRegistry;
  private final RequesterFactory<IpNetwork, UdpRequesterConfiguration> requesterFactory;

  @Activate
  public FatekDiscoveryService(@Reference NetworkInterfaceRegistry networkInterfaceRegistry, @Reference RequesterFactory<IpNetwork, UdpRequesterConfiguration> requesterFactory,
      @Reference DiscoveryCoordinator discoveryCoordinator) {
    super(Arrays.asList(
      FatekBindingConstants.TCP_BRIDGE_TYPE,
      FatekBindingConstants.PLC_THING_TYPE
    ), discoveryCoordinator);
    this.networkInterfaceRegistry = networkInterfaceRegistry;
    this.requesterFactory = requesterFactory;
    this.networkInterfaceRegistry.addCentralNetworkInterfaceStateCallback(this);
  }

  @Override
  protected void startScan() {
    for (Entry<IpNetwork, Requester<InetSocketAddress>> entry : networkDiscoverers.entrySet()) {
      broadcast(entry.getValue());
    }
  }

  @Activate
  public void activate() {
    super.activate(Collections.emptyMap());
    for (NetworkInterface networkInterface : networkInterfaceRegistry.getAll(IpNetworkInterfaceTypes.IP)) {
      startDiscoveryForNetworkInterface(networkInterface);
    }
  }

  @Override
  public void deactivate() {
    for (Entry<IpNetwork, Requester<InetSocketAddress>> entry : networkDiscoverers.entrySet()) {
      try {
        entry.getValue().close();
      } catch (IOException e) {
        logger.warn("Error while stopping UDP requester for network {}", entry.getKey(), e);
      }
    }
    super.deactivate();
  }

  @Override
  public void networkInterfaceUp(NetworkInterface networkInterface) {
    startDiscoveryForNetworkInterface(networkInterface);
  }

  @Override
  public void networkInterfaceDown(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (!(network instanceof IpNetwork)) {
        continue;
      }

      Requester<InetSocketAddress> requester = networkDiscoverers.remove(network);
      if (requester != null) {
        try {
          requester.close();
        } catch (IOException e) {
          logger.warn("Failed to stop udp requester for stopped network interface {} and its network {}", networkInterface, network, e);
        }
      }
    }
  }

  @Override
  public void requestAnswered(InetSocketAddress address, byte[] answer) {
    try {
      Optional<DiscoveryPacket> packet = new DiscoveryPacketParser().parse(answer);
      if (packet.isEmpty()) {
        logger.info("Unsupported discovery packet from {}.", address);
        return;
      }
      DiscoveryPacket discovery = packet.get();
      JFatekTcpFaconConnection faconConnection = new JFatekTcpFaconConnection(
        // connection timeout is also a read timeout, we use small value to make station scan faster
        executor, discovery.getIp(), discovery.getPrimaryFaconPort(), 500
      );

      String bridgeId = zerofill(address.getAddress().getHostAddress());
      DiscoveryResult discoveredBridge = DiscoveryResultBuilder.create(new ThingUID(FatekBindingConstants.TCP_BRIDGE_TYPE, bridgeId))
        .withRepresentationProperty("description")
        .withLabel("Fatek TCP connection '" + discovery.getPlcName() + "' " + address.getHostName() + ":" + discovery.getPrimaryFaconPort())
        .withProperty("hostAddress", discovery.getIp())
        .withProperty("port", discovery.getPrimaryFaconPort())
        .withProperty("description", discovery.getPlcDescription())
        .withProperty(Thing.PROPERTY_FIRMWARE_VERSION, "Firmware V" + discovery.getFirmwareVersion())
        .withProperty(Thing.PROPERTY_MAC_ADDRESS, discovery.getMacAddress())
        .build();
      List<DiscoveryResult> discovered = discover(discoveredBridge, true, faconConnection);
      thingDiscovered(discoveredBridge);
      discovered.forEach(this::thingDiscovered);
      faconConnection.close();
    } catch (IOException e) {
      logger.warn("Failed to parse discovery packet of {} with payload {}.", address, HexUtils.bytesToHex(answer), e);
    }
  }

  private void startDiscoveryForNetworkInterface(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (IpNetworkTypes.IPv4.equals(network.getType()) && network instanceof IpNetwork) {
        IpNetwork ipNetwork = (IpNetwork) network;
        if (ipNetwork.getBroadcastAddress().isEmpty()) {
          continue;
        }

        UdpRequesterConfiguration configuration = new UdpRequesterConfiguration(BROADCAST_PORT, LISTEN_PORT);
        try {
          Requester<InetSocketAddress> requester = requesterFactory.create(ipNetwork, configuration);
          Requester<InetSocketAddress> previous = networkDiscoverers.put(ipNetwork, requester);
          if (previous != null) {
            previous.close();
          }
          requester.setCallback(this);
          requester.start();
          broadcast(requester);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private static void broadcast(Requester<InetSocketAddress> requester) {
    requester.request(DISCOVERY_PACKET_1);
    requester.request(DISCOVERY_PACKET_2);
  }

  private static String zerofill(String hostAddress) {
    String[] digits = hostAddress.split("\\.");
    String ip = "";
    for (String digit : digits) {
      ip += (ip.isEmpty() ? "" : "-") + String.format("%03d", Integer.parseInt(digit));
    }
    return ip;
  }

}
