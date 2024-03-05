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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscovery;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlock;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockAmsNetId;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockHostName;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockPassword;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockRouteName;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsDiscoveryBlockUserName;
import org.apache.plc4x.java.ads.discovery.readwrite.AdsPortNumbers;
import org.apache.plc4x.java.ads.discovery.readwrite.AmsNetId;
import org.apache.plc4x.java.ads.discovery.readwrite.AmsString;
import org.apache.plc4x.java.ads.discovery.readwrite.Operation;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.transport.serial.SerialTransport;
import org.connectorio.addons.binding.amsads.internal.AmsConverter;
import org.connectorio.addons.binding.amsads.internal.config.AmsConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.NetworkConfiguration;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsDiscoveryDriver;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsRouteListener;
import org.connectorio.addons.binding.amsads.internal.discovery.DiscoverySender.Envelope;
import org.connectorio.addons.binding.amsads.internal.handler.channel.ChannelHandlerFactory;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReaderFactory;
import org.connectorio.plc4x.extras.osgi.PlcDriverManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmsAdsNetworkBridgeHandler} is responsible for handling connections to network
 * enabled ADS devices.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class AmsAdsNetworkBridgeHandler extends AbstractAmsAdsThingHandler<AmsBridgeHandler, NetworkConfiguration> implements AmsAdsRouteListener {

  private final Logger logger = LoggerFactory.getLogger(AmsAdsNetworkBridgeHandler.class);
  private final AtomicBoolean routing = new AtomicBoolean(false);

  private final PlcDriverManager driverManager;
  private final AmsAdsDiscoveryDriver discoveryDriver;

  public AmsAdsNetworkBridgeHandler(Thing thing, SymbolReaderFactory symbolReaderFactory, ChannelHandlerFactory channelHandlerFactory,
    PlcDriverManager driverManager, AmsAdsDiscoveryDriver discoveryDriver) {
    super(thing, symbolReaderFactory, channelHandlerFactory);
    this.driverManager = driverManager;
    this.discoveryDriver = discoveryDriver;
  }

  @Override
  public void initialize() {
    if (discoveryDriver != null) {
      discoveryDriver.addRouteListener(this);
    }
    super.initialize();
  }

  @Override
  public void dispose() {
    super.dispose();
    if (discoveryDriver != null) {
      discoveryDriver.removeRouteListener(this);
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  protected Runnable createInitializer(AmsConfiguration amsAds, CompletableFuture<PlcConnection> initializer) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          NetworkConfiguration config = getThingConfig().get();

          if (config.username != null && discoveryDriver != null) {
            AmsNetId sendingAms = AmsConverter.createDiscoveryAms(amsAds.sourceAmsId);
            AmsNetId target = AmsConverter.createDiscoveryAms(amsAds.sourceAmsId);
            List<AdsDiscoveryBlock> blocks = Arrays.asList(
              new AdsDiscoveryBlockRouteName(
                new AmsString("openhab-" + amsAds.ipAddress)
              ),
              new AdsDiscoveryBlockAmsNetId(target),
              new AdsDiscoveryBlockUserName(new AmsString(config.username)),
              new AdsDiscoveryBlockPassword(new AmsString(Optional.ofNullable(config.password).orElse(""))),
              new AdsDiscoveryBlockHostName(new AmsString(amsAds.ipAddress))
            );
            AdsDiscovery request = new AdsDiscovery(1, Operation.ADD_OR_UPDATE_ROUTE_REQUEST, sendingAms, AdsPortNumbers.SYSTEM_SERVICE, blocks);
            logger.info("Making an attempt to setup route from {} to us using {}", config.host, request);
            discoveryDriver.send(new Envelope(config.host, request));

            try {
              // we should coordinate and wait for response from PLC, but real fact is that Beckhoff does not send reply
              // if route is already present - so here is very naive way with delaying initialization. Shame on me.
              Thread.sleep(1750);
            } catch (InterruptedException e) {
              logger.debug("Could not wait for udp answer", e);
            }

            if (routing.get()) {
              logger.info("Route setup successful. Integration should work now.");
            } else {
              logger.warn("Route setup failed. Integration might not work. Please check routing setup on PLC.");
            }
          }

          String target = "target-ams-net-id=" + config.targetAmsId + "&target-ams-port=" + config.targetAmsPort;
          String source = "&source-ams-net-id=" + amsAds.sourceAmsId + "&source-ams-port=" + amsAds.sourceAmsPort;
          String extraOpts = "&load-symbol-and-data-type-tables=false";

          PlcConnection connection = driverManager.getConnectionManager().getConnection("ads:tcp://" + config.host + "?" + target + source + extraOpts);

          if (!connection.isConnected()) {
            connection.connect();
          }

          if (connection.isConnected()) {
            initializer.complete(connection);
          } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection failed");
            initializer.complete(null);
          }
        } catch (PlcRuntimeException e) {
          logger.error("Connection attempt failed due to runtime error", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
          initializer.completeExceptionally(e);
        } catch (PlcConnectionException e) {
          logger.warn("Could not obtain connection", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
          initializer.completeExceptionally(e);
        }
      }
    };
  }

  @Override
  public void add(String host, String sourceAms, boolean success) {
    String targetHost = getThingConfig().map(cfg -> cfg.host).orElse("");
    String targetAms = getThingConfig().map(cfg -> cfg.targetAmsId).orElse("");

    if (targetHost.equals(host) && sourceAms.equals(targetAms)) {
      logger.debug("Received routing setup for ams {}, status: {}", sourceAms, success);
      this.routing.compareAndSet(false, success);
    } else {
      logger.debug("Received routing information {} {}, ignoring", host, sourceAms);
    }
  }

}
