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
package org.connectorio.binding.bacnet.internal.discovery;

import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.MSTP_DEVICE_THING_TYPE;

import java.util.Collections;
import org.code_house.bacnet4j.wrapper.mstp.MstpDevice;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;

public class BACnetMstpDeviceDiscoveryService extends BACnetDeviceDiscoveryService<MstpDevice> implements DiscoveryService {

  private BACnetNetworkBridgeHandler<?> handler;

  public BACnetMstpDeviceDiscoveryService() throws IllegalArgumentException {
    super(Collections.singleton(MSTP_DEVICE_THING_TYPE), 60);
  }

  @Override
  protected void enrich(DiscoveryResultBuilder discoveryResult, MstpDevice device) {
    discoveryResult.withProperty("address", (int) device.getAddress()[0]);
  }

  @Override
  protected ThingUID createThingId(MstpDevice device) {
    final ThingUID bridgeUID = getThingHandler().getThing().getUID();
    return new ThingUID(MSTP_DEVICE_THING_TYPE, bridgeUID, device.getNetworkNumber() + "_" + device.getInstanceNumber());
  }

}
