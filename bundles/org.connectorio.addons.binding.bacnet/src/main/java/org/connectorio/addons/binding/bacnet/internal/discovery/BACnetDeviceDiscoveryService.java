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

import static org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants.IP_DEVICE_THING_TYPE;
import static org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants.MSTP_DEVICE_THING_TYPE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.device.ip.IpDevice;
import org.code_house.bacnet4j.wrapper.device.mstp.MstpDevice;
import org.connectorio.addons.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.connectorio.addons.binding.GenericTypeUtil;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

public class BACnetDeviceDiscoveryService<T extends Device> extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {

  private final Class<T> type;
  private BACnetNetworkBridgeHandler<?> handler;

  @Deprecated
  public BACnetDeviceDiscoveryService(Set<ThingTypeUID> supportedThingsTypes, int timeout) throws IllegalArgumentException {
    super(supportedThingsTypes, timeout);
    this.type = GenericTypeUtil.<T>resolveTypeVariable("T", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve discovery device type"));
  }

  public BACnetDeviceDiscoveryService() {
    super(new HashSet<>(Arrays.asList(IP_DEVICE_THING_TYPE, MSTP_DEVICE_THING_TYPE)), 60);
    this.type = (Class<T>) Device.class;
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  protected void startBackgroundDiscovery() {
    startScan();
  }

  @Override
  protected void startScan() {
    handler.getClient().thenAcceptAsync(cli -> {
      Set<Device> devices = cli.discoverDevices(TimeUnit.SECONDS.toMillis(getScanTimeout()));
      devices.forEach(this::toDiscoveryResult);
    }, scheduler);
  }

  private void toDiscoveryResult(Device device) {
    if (!type.isInstance(device)) {
      return;
    }

    DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(createThingId((T) device))
      .withLabel(device.getModelName() + ", " + device.getName() + " (" + device.getVendorName() + ")" + device.getModelName())
      .withBridge(handler.getThing().getUID())
      .withProperty("instance", device.getInstanceNumber())
      .withProperty("network", device.getBacNet4jAddress().getNetworkNumber().intValue())
      .withRepresentationProperty("address");

    enrich(discoveryResult, (T) device);
    thingDiscovered(discoveryResult.build());
  }

  protected void enrich(DiscoveryResultBuilder discoveryResult, T device) {
    if (device instanceof IpDevice) {
      IpDevice ipDevice = (IpDevice) device;
      discoveryResult.withProperty("address", ipDevice.getHostAddress())
        .withProperty("port", ipDevice.getPort());
    }
    if (device instanceof MstpDevice) {
      discoveryResult.withProperty("address", (int) device.getAddress()[0]);
    }
  }

  protected ThingUID createThingId(T device) {
    final ThingUID bridgeUID = getThingHandler().getThing().getUID();
    if (device instanceof MstpDevice) {
      return new ThingUID(MSTP_DEVICE_THING_TYPE, bridgeUID, device.getNetworkNumber() + "_" + device.getInstanceNumber());
    }
    return new ThingUID(IP_DEVICE_THING_TYPE, bridgeUID, device.getNetworkNumber() + "_" + device.getInstanceNumber());
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof BACnetNetworkBridgeHandler) {
      this.handler = (BACnetNetworkBridgeHandler<?>) handler;
    }
  }

  @Override
  public ThingHandler getThingHandler() {
    return this.handler;
  }

  @Override
  public void activate() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
    super.activate(properties);
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

}
