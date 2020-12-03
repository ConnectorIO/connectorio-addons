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
package org.connectorio.binding.plc4x.canopen.internal.discovery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest.Builder;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.connectorio.binding.plc4x.canopen.internal.handler.CANOpenSocketCANBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CANopenNMTDiscoveryService extends AbstractDiscoveryService implements DiscoveryService,
  ThingHandlerService {

  private static final String SUBSCRIPTION_FIELD_NAME = "nmt-lifecycle-callback";

  private final Logger logger = LoggerFactory.getLogger(CANopenNMTDiscoveryService.class);
  private final Set<Integer> discoveredNodes = new ConcurrentSkipListSet<>();

  private CANOpenSocketCANBridgeHandler handler;
  private PlcConsumerRegistration registration;

  public CANopenNMTDiscoveryService() {
    super(null, 30, true);
  }

  @Override
  protected void startScan() {

  }

  @Override
  protected void startBackgroundDiscovery() {
    handler.getConnection().whenComplete((connection, error) -> {
      if (error != null) {
        logger.info("Could not start background discovery, CAN connection failed", error);
        return;
      }
      final Builder subscriptionBuilder = connection.subscriptionRequestBuilder();
      subscriptionBuilder.addEventField(SUBSCRIPTION_FIELD_NAME, "HEARTBEAT"); // 0 == all nodes

      try {
        PlcSubscriptionResponse response = subscriptionBuilder.build().execute().get();
        PlcResponseCode responseCode = response.getResponseCode(SUBSCRIPTION_FIELD_NAME);
        if (responseCode == PlcResponseCode.OK) {
          registration = response.getSubscriptionHandle(SUBSCRIPTION_FIELD_NAME).register(new NodeStateSubscriptionCallback(SUBSCRIPTION_FIELD_NAME,
            handler, connection, discoveredNodes, new FallbackDiscoveryResultCallback(this::thingDiscovered, handler.getThing().getUID())));
          return;
        }
        logger.warn("Could not subscribe to heartbeat events, returned code: {}", responseCode);
      } catch (InterruptedException e) {
        logger.info("Discovery process have been interrupted", e);
      } catch (ExecutionException e) {
        logger.warn("Failed to subscribe for NMT events", e);
      }
    });
  }

  @Override
  protected void stopBackgroundDiscovery() {
    if (registration != null) {
      registration.unregister();
      discoveredNodes.clear();
    }
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof CANOpenSocketCANBridgeHandler) {
      this.handler = (CANOpenSocketCANBridgeHandler) handler;
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

