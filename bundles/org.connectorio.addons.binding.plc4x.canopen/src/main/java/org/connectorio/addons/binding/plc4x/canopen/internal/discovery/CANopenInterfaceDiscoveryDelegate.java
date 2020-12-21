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
package org.connectorio.addons.binding.plc4x.canopen.internal.discovery;

import java.util.Collections;
import java.util.Set;
import org.connectorio.addons.binding.can.CANInterface;
import org.connectorio.addons.binding.can.CANInterfaceTypes;
import org.connectorio.addons.binding.can.discovery.CANInterfaceDiscoveryDelegate;
import org.connectorio.addons.binding.plc4x.canopen.internal.CANopenBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Basic discovery service which rely on discovery of CAN interfaces.
 */
@Component(service = {DiscoveryService.class, CANInterfaceDiscoveryDelegate.class})
public class CANopenInterfaceDiscoveryDelegate extends AbstractDiscoveryService implements DiscoveryService,
  CANInterfaceDiscoveryDelegate {

  private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(CANopenBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE);

  public CANopenInterfaceDiscoveryDelegate() throws IllegalArgumentException {
    super(SUPPORTED_THING_TYPES, 30, true);
  }

  @Override
  public void interfaceAvailable(CANInterface iface) {
    if (CANInterfaceTypes.SOCKET_CAN.equals(iface.getType())) {
      final String name = iface.getName();
      final DiscoveryResult result = DiscoveryResultBuilder
        .create(new ThingUID(CANopenBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE, name))
        .withLabel("CANopen interface " + name)
        .withProperty("name", name)
        .withRepresentationProperty("name")
        .build();

      thingDiscovered(result);
    }
  }

  @Override
  protected void startScan() {

  }

}
