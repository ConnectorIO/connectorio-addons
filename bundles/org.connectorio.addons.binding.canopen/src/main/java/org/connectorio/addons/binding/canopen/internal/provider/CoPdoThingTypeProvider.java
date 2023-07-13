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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
public class CoPdoThingTypeProvider implements ThingTypeProvider, ConfigDescriptionProvider {

  private static final List<ThingTypeDef> entries = Arrays.asList(
    new ThingTypeDef(CANopenBindingConstants.RECEIVE_PDO_THING_TYPE, "Receive PDO", true,
      // receive is a transit of foreign node
      CANOpenService.TRANSMIT_PDO_1, CANOpenService.TRANSMIT_PDO_2, CANOpenService.TRANSMIT_PDO_3, CANOpenService.TRANSMIT_PDO_4
    ),
    new ThingTypeDef(CANopenBindingConstants.TRANSMIT_PDO_THING_TYPE, "Transmit PDO", false,
      // transmit is receive for other node
      CANOpenService.RECEIVE_PDO_1, CANOpenService.RECEIVE_PDO_2, CANOpenService.RECEIVE_PDO_3, CANOpenService.RECEIVE_PDO_4
    )
  );

  private final Map<ThingTypeUID, ThingType> thingTypes = new HashMap<>();
  private final Map<URI, ConfigDescription> configDescriptors = new HashMap<>();

  public CoPdoThingTypeProvider() {
    for (ThingTypeDef definition : entries) {
      ConfigDescription descriptor = createConfigDescriptor(definition.getType(), definition.getServices());
      configDescriptors.put(descriptor.getUID(), descriptor);

      ThingTypeBuilder typeBuilder = ThingTypeBuilder.instance(definition.getType(), definition.getLabel());
      typeBuilder.withSupportedBridgeTypeUIDs(Arrays.asList(CANopenBindingConstants.NODE_BRIDGE_TYPE.getAsString()));
      typeBuilder.withConfigDescriptionURI(descriptor.getUID());

      List<String> channelIds = new ArrayList<>();
      for (CANOpenDataType dataType : CANOpenDataType.values()) {
        ChannelTypeUID channelType = CoPdoChannelTypeProvider.createChannelTypeUID(definition.isReceivePdo(), dataType);
        channelIds.add(channelType.getId());
      }
      typeBuilder.withExtensibleChannelTypeIds(channelIds);

      thingTypes.put(definition.getType(), typeBuilder.build());
    }
  }

  private ConfigDescription createConfigDescriptor(ThingTypeUID type, List<CANOpenService> services) {
    List<ParameterOption> options = services.stream()
      .map(service -> {
        String label = service.name() + " (addresses 0x" + Integer.toHexString(service.getMin()).toUpperCase() + "-0x" + Integer.toHexString(service.getMax()).toUpperCase() + ")";
        return new ParameterOption(service.name(), label);
      })
      .collect(Collectors.toList());
    URI uri = URI.create(CANopenBindingConstants.BINDING_ID + ":" + type.getId());
    ConfigDescriptionBuilder builder = ConfigDescriptionBuilder.create(uri);
    builder.withParameter(ConfigDescriptionParameterBuilder.create("service", Type.TEXT)
      .withLabel("Service")
      .withDescription("Define base COB ID which is used by this PDO communication")
      .withOptions(options)
      .withRequired(true)
      .build());
    builder.withParameter(ConfigDescriptionParameterBuilder.create("refreshInterval", Type.INTEGER)
      .withLabel("Refresh interval")
      .withDescription("Define time interval within PDO should be received (RPDO) or published (TPDO). Value in milliseconds.")
      .withRequired(true)
      .withDefault("60000")
      .withUnitLabel("ms")
      .build());
    return builder.build();
  }

  @Override
  public Collection<ThingType> getThingTypes(Locale locale) {
    return thingTypes.values();
  }

  @Override
  public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
    return thingTypes.get(thingTypeUID);
  }

  @Override
  public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
    return configDescriptors.values();
  }

  @Override
  public ConfigDescription getConfigDescription(URI uri, Locale locale) {
    return configDescriptors.get(uri);
  }

}
