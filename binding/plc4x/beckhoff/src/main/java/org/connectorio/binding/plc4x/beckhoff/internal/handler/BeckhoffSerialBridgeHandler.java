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
package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.spi.connection.AbstractPlcConnection;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffAmsAdsConfiguration;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffSerialConfiguration;
import org.connectorio.binding.plc4x.shared.osgi.PlcDriverManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BeckhoffSerialBridgeHandler} is responsible for handling connections to Beckhoff PLC
 * over serial port.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class BeckhoffSerialBridgeHandler extends
    BeckhoffBridgeHandler<AbstractPlcConnection, BeckhoffSerialConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(BeckhoffSerialBridgeHandler.class);
  private final PlcDriverManager driverManager;

  public BeckhoffSerialBridgeHandler(Bridge thing, PlcDriverManager driverManager) {
    super(thing);
    this.driverManager = driverManager;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  protected Runnable createInitializer(BeckhoffAmsAdsConfiguration amsAds, CompletableFuture<AbstractPlcConnection> initializer) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          BeckhoffSerialConfiguration config = getBridgeConfig().get();
          String target = hostWithPort(config.targetAmsId, config.targetAmsPort);
          String source = amsAds.sourceAmsId != null && amsAds.sourceAmsPort != null ? "/" + hostWithPort(amsAds.sourceAmsId, amsAds.sourceAmsPort) : "";
          AbstractPlcConnection connection = (AbstractPlcConnection) driverManager.getConnection("ads:serial://" + config.port + "/" + target + source);

          if (!connection.isConnected()) {
            connection.connect();
          }

          if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            initializer.complete(connection);
          } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection failed");
            initializer.complete(null);
          }
        } catch (PlcConnectionException e) {
          logger.warn("Could not obtain connection", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
          initializer.completeExceptionally(e);
        }
      }
    };
  }

}
