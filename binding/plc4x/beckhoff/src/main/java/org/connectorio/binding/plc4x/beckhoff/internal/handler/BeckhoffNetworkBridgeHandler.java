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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.ads.connection.AdsTcpPlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffAmsAdsConfiguration;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffNetworkConfiguration;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.BeckhoffRouteListener;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.DiscoverySender;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.DiscoverySender.Envelope;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.RouteReceiver;
import org.connectorio.binding.plc4x.beckhoff.internal.discovery.udp.UdpRouteRequest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BeckhoffNetworkBridgeHandler} is responsible for handling connections to network
 * enabled ADS devices.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class BeckhoffNetworkBridgeHandler extends BeckhoffBridgeHandler<AdsTcpPlcConnection, BeckhoffNetworkConfiguration> implements
  BeckhoffRouteListener {

  private final Logger logger = LoggerFactory.getLogger(BeckhoffNetworkBridgeHandler.class);
  private final AtomicBoolean routing = new AtomicBoolean(false);

  private final DiscoverySender sender;
  private final RouteReceiver router;

  public BeckhoffNetworkBridgeHandler(Bridge thing, DiscoverySender sender, RouteReceiver router) {
    super(thing);
    this.sender = sender;
    this.router = router;
  }

  @Override
  public void initialize() {
    router.addRouteListener(this);
    super.initialize();
  }

  @Override
  public void dispose() {
    super.dispose();
    router.removeRouteListener(this);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  protected Runnable createInitializer(BeckhoffAmsAdsConfiguration amsAds, CompletableFuture<AdsTcpPlcConnection> initializer) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          BeckhoffNetworkConfiguration config = getBridgeConfig().get();

          if (config.username != null) {
            UdpRouteRequest request = new UdpRouteRequest(amsAds.ipAddress, amsAds.sourceAmsId, amsAds.ipAddress, config.username,
              Optional.ofNullable(config.password).orElse(""));
            logger.info("Making an attempt to setup route from {} to us using {}", config.host, request);
            sender.send(new Envelope(config.host, request));

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

          String host = hostWithPort(config.host, config.port);
          String target = hostWithPort(config.targetAmsId, config.targetAmsPort);
          String source = amsAds.sourceAmsId != null && amsAds.sourceAmsPort != null ? "/" + hostWithPort(amsAds.sourceAmsId, amsAds.sourceAmsPort) : "";
          AdsTcpPlcConnection connection = (AdsTcpPlcConnection) new PlcDriverManager(getClass().getClassLoader())
            .getConnection("ads:tcp://" + host + "/" + target + source);
          //connection.connect();

          if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            initializer.complete(connection);
          } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,"Connection failed");
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
