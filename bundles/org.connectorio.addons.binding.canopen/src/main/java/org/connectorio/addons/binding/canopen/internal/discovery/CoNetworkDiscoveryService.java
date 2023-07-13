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
package org.connectorio.addons.binding.canopen.internal.discovery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;
import org.connectorio.addons.binding.canopen.api.CoConnection;
import org.connectorio.addons.binding.canopen.api.CoSubscription;
import org.connectorio.addons.binding.canopen.internal.handler.CoSocketCANBridgeHandler;
import org.connectorio.plc4x.extras.decorator.phase.PhaseDecorator;
import org.connectorio.plc4x.extras.decorator.retry.RetryDecorator;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoNetworkDiscoveryService extends AbstractDiscoveryService implements DiscoveryService,
  ThingHandlerService, BiConsumer<CoConnection, Throwable> {

  private final Logger logger = LoggerFactory.getLogger(CoNetworkDiscoveryService.class);
  private final Set<Integer> discoveredNodes = new ConcurrentSkipListSet<>();

  private CoSocketCANBridgeHandler handler;
  private CoSubscription subscription;

  public CoNetworkDiscoveryService() {
    super(null, 30, true);
  }

  @Override
  protected void startScan() {

  }

  @Override
  protected void startBackgroundDiscovery() {
    handler.getCoConnection(new PhaseDecorator(), new RetryDecorator(2)).whenComplete(this);
  }

  @Override
  public void accept(CoConnection connection, Throwable error) {
    if (error != null) {
      logger.info("Could not start background discovery, CAN connection failed", error);
      return;
    }

    FallbackDiscoveryResultCallback callback = new FallbackDiscoveryResultCallback(this::thingDiscovered, handler.getThing().getUID());
    connection.heartbeat(0, new NodeStateSubscriptionCallback(handler, connection, discoveredNodes, callback))
      .whenComplete((response, failure) -> {
        if (failure != null) {
          logger.info("Failed to subscribe to NMT/HEARTBEAT information. Discovery of CAN nodes will not work", failure);
          return;
        }
        subscription = response;
      });
  }

  @Override
  protected void stopBackgroundDiscovery() {
    if (subscription != null) {
      subscription.unsubscribe();
      discoveredNodes.clear();
    }
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof CoSocketCANBridgeHandler) {
      this.handler = (CoSocketCANBridgeHandler) handler;
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

  @Override
  protected void thingDiscovered(DiscoveryResult discoveryResult) {
    super.thingDiscovered(discoveryResult);

    Optional.ofNullable(discoveryResult.getProperties().get("nodeId"))
      .filter(Integer.class::isInstance)
      .map(Integer.class::cast)
      .ifPresent(discoveredNodes::add);
  }

  @Override
  protected void removeOlderResults(long timestamp, Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
    discoveredNodes.clear();

    super.removeOlderResults(timestamp, thingTypeUIDs, bridgeUID);
  }

}

