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
package org.connectorio.addons.binding.amsads.internal.discovery;

import java.util.Collections;
import org.connectorio.addons.binding.amsads.AmsAdsBindingConstants;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceRegistry;
import org.connectorio.addons.network.iface.NetworkInterfaceStateCallback;
import org.connectorio.addons.network.ip.IpNetwork;
import org.connectorio.addons.network.ip.IpNetworkInterfaceTypes;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = DiscoveryService.class)
public class AmsAdsInterfaceDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, NetworkInterfaceStateCallback {

  private final NetworkInterfaceRegistry networkInterfaceRegistry;

  @Activate
  public AmsAdsInterfaceDiscoveryService(@Reference NetworkInterfaceRegistry networkInterfaceRegistry) {
    super(Collections.singleton(AmsAdsBindingConstants.THING_TYPE_NETWORK), 60);
    this.networkInterfaceRegistry = networkInterfaceRegistry;
    this.networkInterfaceRegistry.addCentralNetworkInterfaceStateCallback(this);
  }

  @Deactivate
  public void dispose() {
    this.networkInterfaceRegistry.removeCentralNetworkInterfaceStateCallback(this);
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  protected void startScan() {
    for (NetworkInterface networkInterface : networkInterfaceRegistry.getAll(IpNetworkInterfaceTypes.IP)) {
      for (Network network : networkInterface.getNetworks()) {
        if (network instanceof IpNetwork) {
          discover((IpNetwork) network);
        }
      }
    }
  }

  @Override
  protected synchronized void stopScan() {
  }

  @Override
  public void networkInterfaceUp(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (!(network instanceof IpNetwork)) {
        continue;
      }

      IpNetwork ipNetwork = (IpNetwork) network;
      discover(ipNetwork);
    }

  }

  @Override
  public void networkInterfaceDown(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (!(network instanceof IpNetwork)) {
        continue;
      }

      IpNetwork ipNetwork = (IpNetwork) network;
      thingRemoved(createThingUID(ipNetwork.getAddress() + ".1.1"));
    }
  }

  private void discover(IpNetwork ipNetwork) {
    String address = ipNetwork.getAddress();
    String broadcastAddress = ipNetwork.getBroadcastAddress();
    if (!address.isEmpty() && !broadcastAddress.isEmpty()) {
      String amsNetId = address + ".1.1";

      DiscoveryResult network = DiscoveryResultBuilder
        .create(createThingUID(amsNetId))
        .withRepresentationProperty("sourceAmsId")
        .withLabel("Suggested AMS/ADS interface " + address + " (" + amsNetId + ")")
        .withProperty("sourceAmsId", amsNetId)
        .withProperty("ipAddress", address)
        .withProperty("broadcastAddress", broadcastAddress)
        .build();

      thingDiscovered(network);
    }
  }

  private static ThingUID createThingUID(String amsNetId) {
    return new ThingUID(AmsAdsBindingConstants.THING_TYPE_AMS, amsNetId.replace(".", "_"));
  }

}
