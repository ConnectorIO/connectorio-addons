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
package org.connectorio.addons.binding.mbus.internal.discovery;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.binding.mbus.MBusBindingConstants;
import org.connectorio.addons.binding.mbus.config.BridgeConfig;
import org.connectorio.addons.binding.mbus.config.DiscoveryMethod;
import org.connectorio.addons.binding.mbus.internal.handler.MBusBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.SecondaryAddressListener;
import org.openmuc.jmbus.VariableDataStructure;

public class MBusDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService,
    SecondaryAddressListener {

  private final Set<SecondaryAddress> addresses = new CopyOnWriteArraySet<>();
  private MBusBridgeHandler<BridgeConfig> handler;
  private long timeToLive;
  private DiscoveryMethod discoveryMethod;
  private String wildcardMask;

  public MBusDiscoveryService() {
    super(new HashSet<>(Arrays.asList(
      MBusBindingConstants.DEVICE_THING_TYPE
    )), 0, true);
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    this.handler = (MBusBridgeHandler<BridgeConfig>) handler;

    this.timeToLive = this.handler.getBridgeConfig()
      .map(cfg -> cfg.discoveryTimeToLive)
      .filter(val -> val > 0)
      .map(TimeUnit.MINUTES::toSeconds)
      .orElse(DiscoveryResult.TTL_UNLIMITED);
    this.discoveryMethod = this.handler.getBridgeConfig()
      .map(cfg -> cfg.discoveryMethod)
      .orElse(DiscoveryMethod.PRIMARY);
    this.wildcardMask = this.handler.getBridgeConfig()
      .map(cfg -> cfg.wildcardMask)
      .orElse(null);
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  protected void startScan() {
    handler.getConnection().thenAccept(connection -> {
      if (discoveryMethod == DiscoveryMethod.SECONDARY) {
        scanSecondary(connection);
        return;
      }
      scanPrimary(connection);
    });
  }

  private void scanPrimary(MBusConnection connection) {
    for (int nodeId = 1; nodeId < 250; nodeId++) {
      try {
        VariableDataStructure structure = connection.read(nodeId);
        if (structure != null) {
          discover(nodeId, structure);
        }
      } catch (IOException e) {
        // ignore, we might fail
      }
    }
  }

  private void scanSecondary(MBusConnection connection) {
    try {
      connection.scan(wildcardMask, this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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

    }
    super.deactivate();
  }

  private void discover(int nodeId, VariableDataStructure message) {
    handler.getDiscoveryCoordinator().thenApply(coordinator -> {
      DiscoveryResult result = coordinator.discover(handler.getThing().getUID(), timeToLive, message);

      if (result != null) {
        if (nodeId > 0) {
          // append primary address information if defined
          Map<String, Object> properties = result.getProperties();
          properties.put("nodeId", nodeId);
          result = DiscoveryResultBuilder.create(result.getThingUID())
            .withTTL(result.getTimeToLive())
            .withThingType(result.getThingTypeUID())
            .withRepresentationProperty(result.getRepresentationProperty())
            .withProperties(properties)
            .withBridge(result.getBridgeUID())
            .withLabel(result.getLabel())
            .build();
        }

        thingDiscovered(result);
      }
      return result;
    });
  }

  @Override
  public void newScanMessage(String message) {
  }

  @Override
  public void newDeviceFound(SecondaryAddress secondaryAddress) {
    // Turn secondary address into partial variable data structure, its awkward but needed
    // The scan helper in jmbus does not propagate meter answer, it does return only address
    discover(0, new VariableDataStructureStub(secondaryAddress));
  }

  static class VariableDataStructureStub extends VariableDataStructure {
    VariableDataStructureStub(SecondaryAddress secondaryAddress) {
      super(new byte[0], 0, 0, secondaryAddress, Collections.emptyMap());
    }

    @Override
    public void decode() throws DecodingException {
      // do nothing
    }
  }
}
