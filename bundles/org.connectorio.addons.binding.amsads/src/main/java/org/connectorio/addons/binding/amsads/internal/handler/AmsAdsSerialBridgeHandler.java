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
package org.connectorio.addons.binding.amsads.internal.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.connectorio.addons.binding.amsads.internal.config.AmsConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.SerialConfiguration;
import org.connectorio.addons.binding.amsads.internal.handler.channel.ChannelHandlerFactory;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReaderFactory;
import org.connectorio.addons.binding.plc4x.source.SourceFactory;
import org.connectorio.plc4x.extras.osgi.PlcDriverManager;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmsAdsSerialBridgeHandler} is responsible for handling connections to AMS/ADS PLC
 * over serial port.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class AmsAdsSerialBridgeHandler extends AbstractAmsAdsThingHandler<AmsBridgeHandler, SerialConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(AmsAdsSerialBridgeHandler.class);
  private final PlcDriverManager driverManager;

  public AmsAdsSerialBridgeHandler(Thing thing, SymbolReaderFactory symbolReaderFactory, ChannelHandlerFactory channelHandlerFactory,
    PlcDriverManager driverManager, SourceFactory sourceFactory) {
    super(thing, symbolReaderFactory, channelHandlerFactory, sourceFactory);
    this.driverManager = driverManager;
  }

  @Override
  protected Runnable createInitializer(AmsConfiguration amsAds, CompletableFuture<PlcConnection> initializer) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          SerialConfiguration config = getThingConfig().get();
          String target = "targetAmsNetId=" + config.targetAmsId + "&targetAmsPort=" + config.targetAmsPort;
          String source = "&sourceAmsNetId=" + amsAds.sourceAmsId + "&sourceAmsPort=" + amsAds.sourceAmsPort;
          PlcConnection connection = driverManager.getConnectionManager().getConnection("ads:serial://" + config.port + "/" + target + source);

          if (!connection.isConnected()) {
            connection.connect();
          }

          if (connection.isConnected()) {
            initializer.complete(connection);
          } else {
            initializer.completeExceptionally(new TimeoutException("Could not establish connection"));
          }
        } catch (PlcConnectionException e) {
          initializer.completeExceptionally(e);
        }
      }
    };
  }

}
