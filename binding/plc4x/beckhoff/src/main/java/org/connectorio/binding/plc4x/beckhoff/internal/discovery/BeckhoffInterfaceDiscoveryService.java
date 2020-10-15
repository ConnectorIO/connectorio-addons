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
package org.connectorio.binding.plc4x.beckhoff.internal.discovery;

import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_AMS;
import static org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_NETWORK;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.plc4x.java.ads.readwrite.AmsNetId;
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
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = DiscoveryService.class)
public class BeckhoffInterfaceDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, NetworkAddressChangeListener {

  private final NetworkAddressService networkAddressService;

  @Activate
  public BeckhoffInterfaceDiscoveryService(@Reference NetworkAddressService networkAddressService) {
    super(Collections.singleton(THING_TYPE_NETWORK), 60);
    this.networkAddressService = networkAddressService;
  }

  @Override
  protected void startScan() {
    broadcast(NetUtil.getAllInterfaceAddresses());
  }

  @Override
  protected synchronized void stopScan() {
  }

  @Override
  public void onChanged(List<CidrAddress> added, List<CidrAddress> removed) {
    if (!removed.isEmpty()) {
      removeOlderResults(getTimestampOfLastScan());
    }

    broadcast(added);
  }

  private void broadcast(Collection<CidrAddress> broadcast) {
    for (CidrAddress addr : broadcast) {
      InetAddress address = addr.getAddress();
      if (address instanceof Inet4Address) {
        String amsNetId = address.getHostAddress() + ".1.1";

        DiscoveryResult network = DiscoveryResultBuilder
          .create(new ThingUID(THING_TYPE_AMS, amsNetId.replace(".", "_")))
          .withRepresentationProperty("sourceAmsId")
          .withLabel("Suggested AMS/ADS interface " + address.getHostAddress() + " (" + amsNetId + ")")
          .withProperty("sourceAmsId", amsNetId)
          .withProperty("ipAddress", address.getHostAddress())
          .withProperty("broadcastAddress", NetUtil.getIpv4NetBroadcastAddress(address.getHostAddress(), (short) addr.getPrefix()))
          .build();

        thingDiscovered(network);
      }
    }
  }

}
