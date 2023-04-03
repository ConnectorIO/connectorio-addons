/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.wmbus.internal.discovery;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.binding.wmbus.WMBusBindingConstants;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageListener;
import org.connectorio.addons.binding.wmbus.internal.config.OpenHABSerialBridgeConfig;
import org.connectorio.addons.binding.wmbus.internal.handler.WMBusBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusMessage;

public class WMBusDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, WMBusMessageListener {

  private final Set<SecondaryAddress> addresses = new CopyOnWriteArraySet<>();
  private WMBusBridgeHandler<OpenHABSerialBridgeConfig> handler;
  private long timeToLive;

  public WMBusDiscoveryService() {
    super(new HashSet<>(Arrays.asList(
      WMBusBindingConstants.DEVICE_THING_TYPE
    )),0, true);
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    this.handler = (WMBusBridgeHandler<OpenHABSerialBridgeConfig>) handler;
    this.handler.getDispatcher().thenAccept(dsp -> dsp.attach(this));
    this.timeToLive = this.handler.getBridgeConfig()
      .map(cfg -> cfg.discoveryTimeToLive)
      .filter(val -> val > 0)
      .map(TimeUnit.MINUTES::toSeconds)
      .orElse(DiscoveryResult.TTL_UNLIMITED);
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  protected void startScan() {

  }

  @Override
  public ThingHandler getThingHandler() {
    return handler;
  }

  @Override
  public void activate() {
    super.activate(Collections.emptyMap());
  }

  @Override
  public void deactivate() {
    if (this.handler != null) {
      this.handler.getDispatcher().thenAccept(dsp -> dsp.detach(this));
    }
    super.deactivate();
  }

  @Override
  public SecondaryAddress getAddress() {
    return WILDCARD_ADDRESS;
  }

  @Override
  public void onMessage(WMBusMessage message) {
    SecondaryAddress address = message.getSecondaryAddress();
    if (!addresses.contains(address)) {
      discover(message);
      addresses.add(address);
    }
  }

  private void discover(WMBusMessage message) {
    handler.getDiscoveryCoordinator().thenApply(coordinator -> {
      DiscoveryResult result = coordinator.discover(handler.getThing().getUID(), timeToLive, message);
      if (result != null) {
        thingDiscovered(result);
      }
      return result;
    });
  }
}
