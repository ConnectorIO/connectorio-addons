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
package org.connectorio.addons.binding.canbus.internal.discovery;

import java.util.Collections;
import java.util.Set;
import org.connectorio.addons.binding.canbus.CANbusBindingConstants;
import org.connectorio.addons.network.Network;
import org.connectorio.addons.network.NetworkType;
import org.connectorio.addons.network.can.CanNetwork;
import org.connectorio.addons.network.can.CanNetworkInterfaceTypes;
import org.connectorio.addons.network.can.CanNetworkTypes;
import org.connectorio.addons.network.iface.NetworkInterface;
import org.connectorio.addons.network.iface.NetworkInterfaceRegistry;
import org.connectorio.addons.network.iface.NetworkInterfaceStateCallback;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Basic discovery service which rely on network API to discover CAN interface.
 */
@Component(service = {DiscoveryService.class, NetworkInterfaceStateCallback.class}, immediate = true)
public class CANInterfaceDiscoveryDelegate extends AbstractDiscoveryService implements DiscoveryService,
  NetworkInterfaceStateCallback {

  private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(
    CANbusBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE);
  private final NetworkInterfaceRegistry networkInterfaceRegistry;

  @Activate
  public CANInterfaceDiscoveryDelegate(@Reference NetworkInterfaceRegistry networkInterfaceRegistry) throws IllegalArgumentException {
    super(SUPPORTED_THING_TYPES, 30, true);
    this.networkInterfaceRegistry = networkInterfaceRegistry;
  }

  @Override
  protected void startScan() {
    for (NetworkInterface networkInterface : networkInterfaceRegistry.getAll()) {
      if (isCompatible(networkInterface)) {
        discover(networkInterface);
      }
    }
  }

  @Override
  public void networkInterfaceUp(NetworkInterface networkInterface) {
    if (isCompatible(networkInterface)) {
      discover(networkInterface);
    }
  }

  @Override
  public void networkInterfaceDown(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (!(network instanceof CanNetwork)) {
        continue;
      }

      // remove all discovery results which point to networks we just lost
      thingRemoved(createThingUID(networkInterface));
    }
  }

  private void discover(NetworkInterface networkInterface) {
    for (Network network : networkInterface.getNetworks()) {
      if (!(network instanceof CanNetwork)) {
        continue;
      }

      CanNetwork canNetwork = (CanNetwork) network;
      NetworkType networkType = canNetwork.getType();
      if (CanNetworkTypes.CAN_RAW.equals(networkType)) {
        String name = networkInterface.getUID().getName();
        final DiscoveryResult result = DiscoveryResultBuilder
          .create(createThingUID(networkInterface))
          .withLabel("CAN interface " + name)
          .withProperty("name", name)
          .withRepresentationProperty("name")
          .build();

        thingDiscovered(result);
      }
    }
  }

  private static ThingUID createThingUID(NetworkInterface networkInterface) {
    return new ThingUID(CANbusBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE, networkInterface.getUID().getName());
  }

  private static boolean isCompatible(NetworkInterface networkInterface) {
    return CanNetworkInterfaceTypes.SOCKETCAN.equals(networkInterface.getInterfaceType()) ||
      CanNetworkInterfaceTypes.VCAN.equals(networkInterface.getInterfaceType());
  }

}
