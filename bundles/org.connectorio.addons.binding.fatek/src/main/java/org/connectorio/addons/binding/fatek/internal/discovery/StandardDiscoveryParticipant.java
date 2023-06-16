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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.connectorio.addons.binding.fatek.FatekBindingConstants;
import org.connectorio.addons.binding.fatek.discovery.FatekDiscoveryParticipant;
import org.connectorio.addons.binding.fatek.jfatek.DetailedStatus;
import org.connectorio.addons.binding.fatek.jfatek.ReadDetailedStatusCmd;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

@Component(property = {
  Constants.SERVICE_RANKING + "=0"
}, service = FatekDiscoveryParticipant.class)
public class StandardDiscoveryParticipant implements FatekDiscoveryParticipant {

  public Optional<DiscoveryResult> discover(ThingUID bridgeUID, short stationNumber, long timeToLive, FaconConnection connection) {
    DetailedStatus status;
    try {
      status = connection.execute(stationNumber, new ReadDetailedStatusCmd(connection.asFatek())).get();
    } catch (InterruptedException | ExecutionException e) {
      return Optional.empty();
    }

    // We did not find a thing type for this device, so let's treat it as a generic one
    String label = "Fatek FBs-" + status.getPoints().getCount() + status.getUnitType() + " station number:" + status.getStationNo();

    Map<String, Object> properties = new HashMap<>();
    properties.put("stationNumber", stationNumber);
    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, status.getVersion());
    ThingTypeUID typeUID = FatekBindingConstants.PLC_THING_TYPE;

    ThingUID thingUID = new ThingUID(typeUID, bridgeUID, String.valueOf(status.getStationNo()));
    return Optional.of(DiscoveryResultBuilder.create(thingUID)
      .withRepresentationProperty(Thing.PROPERTY_FIRMWARE_VERSION)
      .withThingType(typeUID)
      .withProperties(properties)
      .withLabel(label)
      .withBridge(bridgeUID)
      .withTTL(timeToLive)
      .build()
    );
  }

}
