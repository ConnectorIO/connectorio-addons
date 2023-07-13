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
package org.connectorio.addons.binding.canopen.internal.provider;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.connectorio.addons.binding.canopen.internal.CANopenBindingConstants;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.osgi.service.component.annotations.Component;

@Component
public class CoNodeThingTypeProvider implements ThingTypeProvider, ConfigDescriptionProvider {

  private final ThingType thingType;
  private final ConfigDescription configDescription;

  public CoNodeThingTypeProvider() {
    List<String> channelIds = Arrays.stream(CANOpenDataType.values())
      .map(CoSdoChannelTypeProvider::createChannelTypeUID)
      .map(ChannelTypeUID::getId)
      .collect(Collectors.toList());

    URI configUri = URI.create(CANopenBindingConstants.BINDING_ID + ":" + CANopenBindingConstants.NODE_BRIDGE_TYPE.getId());
    ThingTypeBuilder builder = ThingTypeBuilder.instance(CANopenBindingConstants.NODE_BRIDGE_TYPE, "Generic CANOpen device")
      .withDescription("A generic purpose CANopen node which can be polled for data via SDO requests.")
      .withSupportedBridgeTypeUIDs(Collections.singletonList(CANopenBindingConstants.SOCKETCAN_BRIDGE_THING_TYPE.getAsString()))
      .withConfigDescriptionURI(configUri)
      .withExtensibleChannelTypeIds(channelIds);

    ConfigDescriptionBuilder configBuilder = ConfigDescriptionBuilder.create(configUri);
    configBuilder.withParameter(ConfigDescriptionParameterBuilder.create("nodeId", Type.INTEGER)
      .withMinimum(BigDecimal.ONE)
      .withMaximum(BigDecimal.valueOf(127))
      .withLabel("Node ID")
      .withDescription("Identifier of CANopen node operating over the bus. "
        + "Please be aware that this is not a CAN id. "
        + "Full CAN 2.0A identifier is 11 bits long while CANopen node ID is 7 bits long."
        + "Remaining 4 bits are occupied by CANopen service identifier."
        + "Maximum node id is 127 or less.")
      .withRequired(true)
      .build());
    configBuilder.withParameter(ConfigDescriptionParameterBuilder.create("refreshInterval", Type.INTEGER)
      .withLabel("Refresh interval")
      .withDescription("Time between next poll cycles. If no value is given polling is based on value set on thing or bridge value.")
      .withRequired(true)
      .withDefault("60000")
      .withUnitLabel("ms")
      .build());

    this.configDescription = configBuilder.build();
    this.thingType = builder.buildBridge();
  }

  @Override
  public Collection<ThingType> getThingTypes(Locale locale) {
    return Collections.singleton(thingType);
  }

  @Override
  public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
    if (thingTypeUID.equals(thingType.getUID())) {
      return thingType;
    }
    return null;
  }

  @Override
  public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
    return Collections.singleton(configDescription);
  }

  @Override
  public ConfigDescription getConfigDescription(URI uri, Locale locale) {
    if (uri.equals(configDescription.getUID())) {
      return configDescription;
    }
    return null;
  }

}
