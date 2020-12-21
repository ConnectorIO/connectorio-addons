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
package org.connectorio.addons.binding.plc4x.beckhoff.internal.discovery;

import static org.connectorio.addons.binding.plc4x.beckhoff.internal.AmsConverter.createDiscoveryAms;
import static org.connectorio.addons.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants.THING_TYPE_NETWORK;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.plc4x.java.ads.discovery.readwrite.DiscoveryRequest;
import org.apache.plc4x.java.ads.discovery.readwrite.types.Direction;
import org.apache.plc4x.java.ads.discovery.readwrite.types.Operation;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.config.BeckhoffAmsAdsConfiguration;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.handler.BeckhoffAmsAdsBridgeHandler;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.discovery.DiscoverySender.Envelope;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;

public class BeckhoffDeviceDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService,
  BeckhoffDiscoveryListener {

  private ThingHandler handler;

  public BeckhoffDeviceDiscoveryService() throws IllegalArgumentException {
    super(Collections.singleton(BeckhoffBindingConstants.THING_TYPE_NETWORK), 60);
  }

  @Override
  protected void startScan() {
    getHandler().map(BeckhoffAmsAdsBridgeHandler::getSender)
      .ifPresent(sender -> {
        BeckhoffAmsAdsConfiguration cfg = getConfig().get();

        sender.send(new Envelope(
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
    getHandler().map(BeckhoffAmsAdsBridgeHandler::getReceiver)
      .ifPresent(receiver -> receiver.addDiscoveryListener(this));
  }

  @Override
  public void deactivate() {
    getHandler().map(BeckhoffAmsAdsBridgeHandler::getReceiver)
      .ifPresent(receiver -> receiver.removeDiscoveryListener(this));
  }

  private Optional<BeckhoffAmsAdsConfiguration> getConfig() {
    if (handler instanceof BeckhoffAmsAdsBridgeHandler) {
      return ((BeckhoffAmsAdsBridgeHandler) handler).getBridgeConfig();
    }
    return Optional.empty();
  }

  private Optional<BeckhoffAmsAdsBridgeHandler> getHandler() {
    if (handler instanceof BeckhoffAmsAdsBridgeHandler) {
      return Optional.of(((BeckhoffAmsAdsBridgeHandler) handler));
    }
    return Optional.empty();
  }

  @Override
  public void deviceDiscovered(String host, String name, String amsNetId) {
    final ThingUID bridgeUID = getHandler().get().getThing().getUID();
    DiscoveryResult network = DiscoveryResultBuilder
      .create(new ThingUID(THING_TYPE_NETWORK, bridgeUID, host.replace(".", "_")))
      .withLabel("Beckhoff device " + name + " (" + amsNetId + ")")
      .withProperty("targetAmsId", amsNetId)
      .withProperty("host", host)
      .withBridge(bridgeUID)
      .build();

    thingDiscovered(network);
  }

}
