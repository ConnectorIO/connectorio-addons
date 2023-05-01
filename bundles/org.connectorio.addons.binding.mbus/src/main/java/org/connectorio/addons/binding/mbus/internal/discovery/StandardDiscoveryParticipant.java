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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.connectorio.addons.binding.mbus.MBusBindingConstants;
import org.connectorio.addons.binding.mbus.discovery.MBusDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

@Component(property = {
  Constants.SERVICE_RANKING + "=0"
}, service = MBusDiscoveryParticipant.class)
public class StandardDiscoveryParticipant implements MBusDiscoveryParticipant {

  public Optional<DiscoveryResult> discover(ThingUID bridgeUID, long timeToLive, WMBusMessage message) {
    SecondaryAddress secondaryAddress = message.getSecondaryAddress();
    int deviceId = secondaryAddress.getDeviceId().intValue();
    DeviceType deviceType = secondaryAddress.getDeviceType();

    // We did not find a thing type for this device, so let's treat it as a generic one
    String label = "M-Bus device " + deviceType.name().toLowerCase().replace("_", " ") +
      " #" + deviceId + " (" + deviceType + ")";

    Map<String, Object> properties = new HashMap<>();
    properties.put(Thing.PROPERTY_VENDOR, secondaryAddress.getManufacturerId());
    properties.put(Thing.PROPERTY_HARDWARE_VERSION, secondaryAddress.getVersion());
    properties.put(Thing.PROPERTY_SERIAL_NUMBER, secondaryAddress.getDeviceId().toString());
    properties.put("version", secondaryAddress.getVersion());
    properties.put("deviceType", deviceType.name());
    properties.put("manufacturerId", secondaryAddress.getManufacturerId());
    ThingTypeUID typeUID = MBusBindingConstants.DEVICE_THING_TYPE;

    ThingUID thingUID = new ThingUID(typeUID, bridgeUID, String.valueOf(deviceId));
    return Optional.of(DiscoveryResultBuilder.create(thingUID)
      .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
      .withThingType(typeUID)
      .withProperties(properties)
      .withLabel(label)
      .withBridge(bridgeUID)
      .withTTL(timeToLive)
      .build()
    );
  }

  @Override
  public Optional<DiscoveryResult> discover(ThingUID bridgeUID, long timeToLive, VariableDataStructure message) {
    return Optional.empty();
  }
}
