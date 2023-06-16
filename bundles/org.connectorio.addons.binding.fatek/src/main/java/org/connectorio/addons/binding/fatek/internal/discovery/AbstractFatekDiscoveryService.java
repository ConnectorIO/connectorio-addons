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
package org.connectorio.addons.binding.fatek.internal.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFatekDiscoveryService extends AbstractDiscoveryService {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  protected final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "Fatek #" + getClass().getName()));
  private final DiscoveryCoordinator discoveryCoordinator;

  public AbstractFatekDiscoveryService(List<ThingTypeUID> thingTypes, DiscoveryCoordinator discoveryCoordinator) {
    super(new HashSet<>(thingTypes), 0, true);
    this.discoveryCoordinator = discoveryCoordinator;
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  protected void startScan() {

  }

  protected final List<DiscoveryResult> discover(DiscoveryResult discoveredBridge, boolean single, FaconConnection connection) {
    // scan possible station numbers to find all configured combinations
    List<DiscoveryResult> results = new ArrayList<>();
    for (short stationNumber = 1; stationNumber < 255; stationNumber++) {
      logger.debug("Checking facon connection to {} with station {}", discoveredBridge, stationNumber);
      DiscoveryResult result = discoveryCoordinator.discover(discoveredBridge.getThingUID(), stationNumber, connection, DiscoveryResult.TTL_UNLIMITED);
      if (result != null) {
        results.add(result);
        if (single) {
          break;
        }
      }
    }

    return results;
  }

  protected final void announce(List<DiscoveryResult> results) {
    for (DiscoveryResult result : results) {
      thingDiscovered(result);
    }
  }

}
