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
package org.connectorio.addons.binding.bacnet.internal.handler.property;

import com.serotonin.bacnet4j.obj.DeviceObject;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.connectorio.addons.binding.bacnet.internal.discovery.BACnetPropertyDiscoveryService;
import org.connectorio.addons.binding.bacnet.internal.config.DeviceConfig;
import org.connectorio.addons.binding.bacnet.internal.handler.BACnetObjectHandler;
import org.connectorio.addons.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

public abstract class BACnetDeviceHandler<C extends DeviceConfig> extends BACnetObjectHandler<DeviceObject, BACnetNetworkBridgeHandler<?>, C>
  implements BACnetDeviceBridgeHandler<BACnetNetworkBridgeHandler<?>, C> {

  private Device device;

  /**
   * Creates a new instance of this class for the {@link Thing}.
   *
   * @param bridge the thing that should be handled, not null
   */
  public BACnetDeviceHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<BACnetNetworkBridgeHandler<?>> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(BACnetNetworkBridgeHandler.class::isInstance)
      .map(BACnetNetworkBridgeHandler.class::cast);
  }

  @Override
  public void initialize() {
    device = getBridgeConfig()
      .map(cfg -> {
        Integer networkNumber =  Optional.ofNullable(cfg.network)
          .orElseGet(() -> getBridgeHandler().flatMap(BACnetNetworkBridgeHandler::getNetworkNumber).orElse(0));
        return createDevice(cfg, networkNumber);
      }).orElse(null);

    if (device != null) {
      updateStatus(ThingStatus.ONLINE);
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing device configuration");
    }
  }

  protected abstract Device createDevice(C config, Integer networkNumber);

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(BACnetPropertyDiscoveryService.class);
  }

  @Override
  public Optional<CompletableFuture<BacNetClient>> getClient() {
    return getBridgeHandler().map(BACnetNetworkBridgeHandler::getClient);
  }

  @Override
  public Device getDevice() {
    return device;
  }

}
