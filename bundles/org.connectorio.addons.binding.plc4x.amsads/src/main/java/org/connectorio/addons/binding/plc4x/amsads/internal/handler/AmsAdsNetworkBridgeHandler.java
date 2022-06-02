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
package org.connectorio.addons.binding.plc4x.amsads.internal.handler;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.plc4x.java.ads.discovery.readwrite.AmsMagicString;
import org.apache.plc4x.java.ads.discovery.readwrite.AmsNetId;
import org.apache.plc4x.java.ads.discovery.readwrite.RouteRequest;
import org.apache.plc4x.java.ads.discovery.readwrite.types.Direction;
import org.apache.plc4x.java.ads.discovery.readwrite.types.Operation;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.connectorio.addons.binding.plc4x.amsads.internal.AmsConverter;
import org.connectorio.addons.binding.plc4x.amsads.internal.config.AmsAdsConfiguration;
import org.connectorio.addons.binding.plc4x.amsads.internal.config.NetworkConfiguration;
import org.connectorio.addons.binding.plc4x.amsads.internal.discovery.AmsAdsDiscoveryDriver;
import org.connectorio.addons.binding.plc4x.amsads.internal.discovery.AmsAdsRouteListener;
import org.connectorio.addons.binding.plc4x.amsads.internal.discovery.DiscoverySender;
import org.connectorio.addons.binding.plc4x.amsads.internal.discovery.DiscoverySender.Envelope;
import org.connectorio.addons.binding.plc4x.amsads.internal.discovery.RouteReceiver;
import org.connectorio.plc4x.extras.osgi.PlcDriverManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
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
public class AmsAdsNetworkBridgeHandler extends AbstractAmsAdsBridgeHandler<PlcConnection, NetworkConfiguration> implements
  AmsAdsRouteListener {

  private final Logger logger = LoggerFactory.getLogger(AmsAdsNetworkBridgeHandler.class);
  private final AtomicBoolean routing = new AtomicBoolean(false);

  private final PlcDriverManager driverManager;
  private final AmsAdsDiscoveryDriver discoveryDriver;

  public AmsAdsNetworkBridgeHandler(Bridge thing, PlcDriverManager driverManager, AmsAdsDiscoveryDriver discoveryDriver) {
    super(thing);
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
  protected Runnable createInitializer(AmsAdsConfiguration amsAds, CompletableFuture<PlcConnection> initializer) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          NetworkConfiguration config = getBridgeConfig().get();

          if (config.username != null && discoveryDriver != null) {
            AmsNetId sendingAms = AmsConverter.createDiscoveryAms(amsAds.sourceAmsId);
            AmsMagicString routeName = new AmsMagicString(("openhab-" + amsAds.ipAddress).getBytes(StandardCharsets.UTF_8));
            AmsNetId target = AmsConverter.createDiscoveryAms(amsAds.sourceAmsId);
            AmsMagicString username = new AmsMagicString(config.username.getBytes(StandardCharsets.UTF_8));
            AmsMagicString password = new AmsMagicString(Optional.ofNullable(config.password).orElse("").getBytes(StandardCharsets.UTF_8));
            AmsMagicString address = new AmsMagicString(amsAds.ipAddress.getBytes(StandardCharsets.UTF_8));
            RouteRequest request = new RouteRequest(Operation.ROUTE, Direction.REQUEST,
              sendingAms, routeName, target, username, password, address);
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

          String target = "targetAmsNetId=" + config.targetAmsId + "&targetAmsPort=" + config.targetAmsPort;
          String source = "&sourceAmsNetId=" + amsAds.sourceAmsId + "&sourceAmsPort=" + amsAds.sourceAmsPort;

          PlcConnection connection = driverManager.getConnection("ads:tcp://" + config.host + "?" + target + source);

          if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
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
    String targetHost = getBridgeConfig().map(cfg -> cfg.host).orElse("");
    String targetAms = getBridgeConfig().map(cfg -> cfg.targetAmsId).orElse("");

    if (targetHost.equals(host) && sourceAms.equals(targetAms)) {
      logger.debug("Received routing setup for ams {}, status: {}", sourceAms, success);
      this.routing.compareAndSet(false, success);
    } else {
      logger.debug("Received routing information {} {}, ignoring", host, sourceAms);
    }
  }

}
