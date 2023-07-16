/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.discovery;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceRegistry;
import org.connectorio.addons.network.iface.NetworkInterfaceStateCallback;
import org.connectorio.addons.network.ip.IpNetwork;
import org.connectorio.addons.network.ip.IpNetworkInterfaceTypes;
import org.connectorio.addons.network.ip.IpNetworkTypes;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = {DiscoveryService.class}, configurationPid = "discovery.bacnet.interface")
public class BACnetInterfaceDiscoveryService extends AbstractDiscoveryService implements
    NetworkInterfaceStateCallback {

  private final Logger logger = LoggerFactory.getLogger(BACnetInterfaceDiscoveryService.class);
  private final NetworkInterfaceRegistry networkInterfaceRegistry;

  @Activate
  public BACnetInterfaceDiscoveryService(@Reference NetworkInterfaceRegistry networkInterfaceRegistry)
    throws IllegalArgumentException {
    super(Collections.singleton(BACnetBindingConstants.IPV4_BRIDGE_THING_TYPE), 60, true);
    this.networkInterfaceRegistry = networkInterfaceRegistry;
    this.networkInterfaceRegistry.addCentralNetworkInterfaceStateCallback(this);
  }

  @Override
  @Deactivate
  public void deactivate() {
    logger.debug("Deactivating BACnet network interface discovery service");
    networkInterfaceRegistry.removeCentralNetworkInterfaceStateCallback(this);
  }

  @Override
  protected void startScan() {
    for (NetworkInterface networkInterface : networkInterfaceRegistry.getAll()) {
      if (IpNetworkInterfaceTypes.IP.equals(networkInterface.getInterfaceType())) {
        discover(networkInterface);
      }
    }
  }

  @Override
  public void networkInterfaceUp(NetworkInterface networkInterface) {
    if (IpNetworkInterfaceTypes.IP.equals(networkInterface.getInterfaceType())) {
      discover(networkInterface);
    }
  }

  @Override
  public void networkInterfaceDown(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (!(network instanceof IpNetwork)) {
        continue;
      }

      // remove all discovery results which point to networks we just lost
      IpNetwork ipNetwork = (IpNetwork) network;
      String broadcastAddress = ipNetwork.getBroadcastAddress();
      if (!broadcastAddress.isEmpty()) {
        thingRemoved(createThingUID(broadcastAddress));
      }
    }
  }

  private void discover(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (!(network instanceof IpNetwork)) {
        continue;
      }

      IpNetwork ipNetwork = (IpNetwork) network;
      String broadcastAddress = ipNetwork.getBroadcastAddress();
      if (!broadcastAddress.isEmpty()) {
        DiscoveryResult ip4network = DiscoveryResultBuilder.create(createThingUID(broadcastAddress))
          .withLabel("BACnet IPv4 network " + broadcastAddress)
          //.withProperty("localBindAddress", address.getHostAddress())
          .withProperty("broadcastAddress", broadcastAddress)
          .build();
        thingDiscovered(ip4network);
      }
    }
  }

  private static ThingUID createThingUID(String broadcastAddress) {
    return new ThingUID(BACnetBindingConstants.IPV4_BRIDGE_THING_TYPE, broadcastAddress.replace(".", "_"));
  }

}
