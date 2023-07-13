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
package org.connectorio.addons.binding.amsads.internal.discovert;

import static org.connectorio.addons.binding.amsads.internal.AmsConverter.createDiscoveryAms;
import static org.connectorio.addons.binding.amsads.AmsAdsBindingConstants.THING_TYPE_NETWORK;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.plc4x.java.ads.discovery.readwrite.DiscoveryRequest;
import org.apache.plc4x.java.ads.discovery.readwrite.types.Direction;
import org.apache.plc4x.java.ads.discovery.readwrite.types.Operation;
import org.connectorio.addons.binding.amsads.internal.config.AmsAdsConfiguration;
import org.connectorio.addons.binding.amsads.internal.handler.AmsAdsBridgeHandler;
import org.connectorio.addons.binding.amsads.AmsAdsBindingConstants;
import org.connectorio.addons.binding.amsads.internal.discovert.DiscoverySender.Envelope;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

public class AmsAdsDeviceDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService,
  AmsAdsDiscoveryListener {

  private ThingHandler handler;

  public AmsAdsDeviceDiscoveryService() throws IllegalArgumentException {
    super(Collections.singleton(AmsAdsBindingConstants.THING_TYPE_NETWORK), 60);
  }

  @Override
  protected void startScan() {
    getHandler().map(AmsAdsBridgeHandler::getDiscoveryDriver)
      .ifPresent(discoveryDriver -> {
        AmsAdsConfiguration cfg = getConfig().get();

        discoveryDriver.send(new Envelope(
          cfg.broadcastAddress,
          new DiscoveryRequest(Operation.DISCOVERY, Direction.REQUEST, createDiscoveryAms(cfg.sourceAmsId))
        ));
      });
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    this.handler = handler;
  }

  @Override
  public ThingHandler getThingHandler() {
    return handler;
  }

  @Override
  protected void activate(Map<String, Object> configProperties) {
    super.activate(configProperties);
  }

  @Override
  public void activate() {
    getHandler().map(AmsAdsBridgeHandler::getDiscoveryDriver)
      .ifPresent(receiver -> receiver.addDiscoveryListener(this));
  }

  @Override
  public void deactivate() {
    getHandler().map(AmsAdsBridgeHandler::getDiscoveryDriver)
      .ifPresent(receiver -> receiver.removeDiscoveryListener(this));
  }

  private Optional<AmsAdsConfiguration> getConfig() {
    if (handler instanceof AmsAdsBridgeHandler) {
      return ((AmsAdsBridgeHandler) handler).getBridgeConfig();
    }
    return Optional.empty();
  }

  private Optional<AmsAdsBridgeHandler> getHandler() {
    if (handler instanceof AmsAdsBridgeHandler) {
      return Optional.of(((AmsAdsBridgeHandler) handler));
    }
    return Optional.empty();
  }

  @Override
  public void deviceDiscovered(String host, String name, String amsNetId) {
    final ThingUID bridgeUID = getHandler().get().getThing().getUID();
    DiscoveryResult network = DiscoveryResultBuilder
      .create(new ThingUID(THING_TYPE_NETWORK, bridgeUID, host.replace(".", "_")))
      .withLabel("AMS/ADS device " + name + " (" + amsNetId + ")")
      .withProperty("targetAmsId", amsNetId)
      .withProperty("host", host)
      .withBridge(bridgeUID)
      .build();

    thingDiscovered(network);
  }

}
