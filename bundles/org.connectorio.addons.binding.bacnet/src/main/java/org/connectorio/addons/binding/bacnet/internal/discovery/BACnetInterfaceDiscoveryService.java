/*
 * Copyright (C) 2019-2020 ConnectorIO sp. z o.o.
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
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.net.NetworkAddressChangeListener;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = {DiscoveryService.class}, configurationPid = "discovery.bacnet.interface")
public class BACnetInterfaceDiscoveryService extends AbstractDiscoveryService implements NetworkAddressChangeListener {

  private final Logger logger = LoggerFactory.getLogger(BACnetInterfaceDiscoveryService.class);
  private final NetworkAddressService networkAddressService;

  @Activate
  public BACnetInterfaceDiscoveryService(@Reference NetworkAddressService networkAddressService)
    throws IllegalArgumentException {
    super(Collections.singleton(BACnetBindingConstants.IPV4_BRIDGE_THING_TYPE), 60, true);
    this.networkAddressService = networkAddressService;

    networkAddressService.addNetworkAddressChangeListener(this);
  }

  @Override
  @Deactivate
  public void deactivate() {
    logger.debug("Deactivating BACnet network interface discovery service");
    networkAddressService.removeNetworkAddressChangeListener(this);
  }

  @Override
  protected void startScan() {
    Collection<CidrAddress> addresses = NetUtil.getAllInterfaceAddresses();
    discover(addresses);
  }

  @Override
  public void onChanged(List<CidrAddress> added, List<CidrAddress> removed) {
    if (!removed.isEmpty()) {
      removeOlderResults(getTimestampOfLastScan());
    }

    discover(added);
  }

  private void discover(Collection<CidrAddress> addresses) {
    for (CidrAddress addr : addresses) {
      InetAddress address = addr.getAddress();
      if (address instanceof Inet4Address) {
        String broadcastAddress = NetUtil.getIpv4NetBroadcastAddress(address.getHostAddress(), (short) addr.getPrefix());

        DiscoveryResult network = DiscoveryResultBuilder.create(new ThingUID(BACnetBindingConstants.IPV4_BRIDGE_THING_TYPE, broadcastAddress.replace(".", "_")))
          .withLabel("BACnet IPv4 network " + address.getHostAddress())
          //.withProperty("localBindAddress", address.getHostAddress())
          .withProperty("broadcastAddress", broadcastAddress)
          .build();
        thingDiscovered(network);
      }
    }
  }

}
