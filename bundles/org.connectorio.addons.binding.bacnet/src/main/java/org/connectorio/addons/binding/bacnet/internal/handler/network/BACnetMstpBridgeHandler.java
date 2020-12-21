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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.mstp.BacNetMstpClient;
import org.code_house.bacnet4j.wrapper.mstp.JsscMstpNetworkBuilder;
import org.code_house.bacnet4j.wrapper.mstp.MstpNetworkBuilder;
import org.connectorio.addons.binding.bacnet.internal.discovery.BACnetMstpDeviceDiscoveryService;
import org.connectorio.addons.binding.bacnet.internal.handler.network.mstp.ManagedMstpNetworkBuilder;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.addons.binding.bacnet.internal.config.MstpConfig;
import org.connectorio.addons.binding.bacnet.internal.config.MstpConfig.Parity;
import org.connectorio.addons.binding.handler.polling.common.BasePollingBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.io.transport.serial.SerialPortManager;

public class BACnetMstpBridgeHandler extends BasePollingBridgeHandler<MstpConfig> implements BACnetNetworkBridgeHandler<MstpConfig> {

  private final SerialPortManager serialPortManager;

  private CompletableFuture<BacNetClient> clientFuture = new CompletableFuture<>();
  private BacNetClient client;

  /**
   * Creates a new instance of this class for the {@link Bridge}.
   *
   * @param bridge the bridge that should be handled, not null.
   * @param serialPortManager serial port manager which is used in case of mstp connections.
   */
  public BACnetMstpBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
    super(bridge);
    this.serialPortManager = serialPortManager;
  }

  @Override
  public void initialize() {
    MstpNetworkBuilder builder = getBridgeConfig().map(config -> {
      Parity parity = config.parity;
      return new ManagedMstpNetworkBuilder(serialPortManager)
        .withSerialPort(config.serialPort)
        .withStation(config.station)
        .withBaud(config.baudRate)
        .withDataBits((short) parity.getDataBits())
        .withParity((short) parity.getParity())
        .withStopBits((short) parity.getStopBits());
    }).orElse(new JsscMstpNetworkBuilder());

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
      try {
        BacNetMstpClient cli = new BacNetMstpClient(builder.build(), getLocalDeviceId().orElse(1339));
        cli.start();
        clientFuture.complete(cli);
      } catch (Exception e) {
        clientFuture.completeExceptionally(e);
      }
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
    return Collections.singleton(BACnetMstpDeviceDiscoveryService.class);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

}
