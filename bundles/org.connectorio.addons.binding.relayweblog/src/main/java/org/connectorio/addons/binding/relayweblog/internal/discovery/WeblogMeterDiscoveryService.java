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
package org.connectorio.addons.binding.relayweblog.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterInfo;
import org.connectorio.addons.binding.relayweblog.internal.handler.WeblogBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

public class WeblogMeterDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

  private WeblogBridgeHandler handler;

  public WeblogMeterDiscoveryService() {
    super(Collections.singleton(RelayWeblogBindingConstants.METER_THING_TYPE), 30);
  }

  @Override
  protected void startScan() {
    Optional.ofNullable(handler).flatMap(WeblogBridgeHandler::getClient)
      .ifPresent(client -> {
        List<MeterInfo> meters = client.getMeters();
        for (MeterInfo meter : meters) {
          ThingUID id = new ThingUID(RelayWeblogBindingConstants.METER_THING_TYPE, handler.getThing().getUID(), meter.getIdentifier());
          DiscoveryResult result = DiscoveryResultBuilder.create(id)
            .withBridge(handler.getThing().getUID())
            .withRepresentationProperty("id")
            .withProperty("INT_NAME", meter.getIntName())
            .withProperty("GROUP", meter.getGroup())
            .withProperty("id", meter.getId())
            .withProperty("TXT1", meter.getText1())
            .withProperty("TXT2", meter.getText1())
            .withProperty(Thing.PROPERTY_VENDOR, meter.getManufacturer())
            .withProperty(Thing.PROPERTY_HARDWARE_VERSION, meter.getVersion())
            .withProperty("meter.identifier", meter.getIdentifier())
            .withProperty("meter.medium", meter.getMedium())
            .withLabel(createLabel(meter))
            .build();
          thingDiscovered(result);
        }
      });
  }

  private String createLabel(MeterInfo meter) {
    String label = meter.getMedium() + " " + meter.getManufacturer() + "@" + meter.getIdentifier();
    boolean suffix = false;
    if (meter.getText1() != null && !meter.getText1().isEmpty()) {
      label += " " + meter.getText1();
      suffix = true;
    }
    if (meter.getText2() != null && !meter.getText2().isEmpty()) {
      label += " " + meter.getText2();
      suffix = true;
    }
    if (!suffix) {
      label += " ver." + meter.getVersion();
    }
    return label;
  }

  @Override
  protected void startBackgroundDiscovery() {
    startScan();
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof WeblogBridgeHandler) {
      this.handler = (WeblogBridgeHandler) handler;
    }
  }

  @Override
  public ThingHandler getThingHandler() {
    return handler;
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

}
