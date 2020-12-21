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
package org.connectorio.addons.binding.bacnet.internal.handler.network;

import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.ip.BacNetIpClient;
import org.connectorio.addons.binding.bacnet.internal.discovery.BACnetIpDeviceDiscoveryService;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.addons.binding.bacnet.internal.config.Ipv4Config;
import org.connectorio.addons.binding.handler.polling.common.BasePollingBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

public class BACnetIpv4BridgeHandler extends BasePollingBridgeHandler<Ipv4Config> implements BACnetNetworkBridgeHandler<Ipv4Config> {

  private CompletableFuture<BacNetClient> clientFuture = new CompletableFuture<>();
  private BacNetClient client;

  /**
   * Creates a new instance of this class for the {@link Bridge}.
   *
   * @param bridge the bridge that should be handled, not null
   */
  public BACnetIpv4BridgeHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  public void initialize() {
    IpNetworkBuilder builder = getBridgeConfig().map(config -> {
      return new IpNetworkBuilder()
        .withBroadcast(config.broadcastAddress, 24)
        //.withLocalBindAddress(config.localBindAddress)
        .withPort(config.port)
        .withLocalNetworkNumber(config.localNetworkNumber)
        .withReuseAddress(true)
        ;
    }).orElse(new IpNetworkBuilder());

    clientFuture.handleAsync((c, e) -> {
      if (e != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
      } else {
        updateStatus(ThingStatus.ONLINE);
      }
      return null;
    }, scheduler);
    clientFuture.thenAcceptAsync(c -> this.client = c, scheduler);

    scheduler.submit(() -> {
      BacNetIpClient cli = new BacNetIpClient(builder.build(), getLocalDeviceId().orElse(1339));
      cli.start();
      clientFuture.complete(cli);
    });
  }

  @Override
  public void dispose() {
    if (client != null) {
      client.stop();
    }

    clientFuture.cancel(true);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  public CompletableFuture<BacNetClient> getClient() {
    return clientFuture;
  }

  @Override
  public Optional<Integer> getNetworkNumber() {
    return getBridgeConfig().map(cfg -> cfg.localNetworkNumber);
  }

  public Optional<Integer> getLocalDeviceId() {
    return getBridgeConfig().map(cfg -> cfg.localDeviceId);
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(BACnetIpDeviceDiscoveryService.class);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

}
