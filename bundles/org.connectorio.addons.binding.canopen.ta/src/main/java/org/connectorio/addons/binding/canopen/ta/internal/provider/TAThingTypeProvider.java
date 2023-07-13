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
package org.connectorio.addons.binding.canopen.ta.internal.provider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelDefinitionBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.connectorio.addons.binding.canopen.ta.internal.TACANopenBindingConstants.*;

@Component
public class TAThingTypeProvider implements ThingTypeProvider, ConfigDescriptionProvider {

  private final static List<ThingTypeDef> entries = new ArrayList<>(Arrays.asList(
//    new ThingTypeDef(TA_ANALOG_TEMPERATURE_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_LENGTH_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_VOLUME_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_INTENSITY_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_TIME_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_POWER_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_ENERGY_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_ELECTRIC_POTENTIAL_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_ELECTRIC_CURRENT_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_ELECTRIC_RESISTANCE_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_SPEED_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_VOLUMETRIC_FLOW_RATE_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_PRESSURE_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_FREQUENCY_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_ANGLE_THING_TYPE),
//    new ThingTypeDef(TA_ANALOG_RAS_THING_TYPE, Arrays.asList(
//      new ChannelTypeUID(BINDING_ID, TA_ANALOG_RAS_MODE), // mode
//      new ChannelTypeUID(BINDING_ID, TA_ANALOG_TEMPERATURE)
//    )),
//    new ThingTypeDef(TA_DIGITAL_SWITCH_CHANNEL_TYPE)
  ));

  private final Logger logger = LoggerFactory.getLogger(TAThingTypeProvider.class);

  private final Map<ThingTypeUID, ThingType> thingTypes = new HashMap<>();
  private final Map<URI, ConfigDescription> configDescriptions = new HashMap<>();
  private final ChannelTypeProvider channelTypeProvider;

  @Activate
  public TAThingTypeProvider(@Reference(target = "(ta=true)") ChannelTypeProvider channelTypeProvider) {
    this.channelTypeProvider = channelTypeProvider;
    for (ThingTypeDef entry : entries) {
      TAChannelTypeProvider.ConfigUID configUID = new TAChannelTypeProvider.ConfigUID(entry.getChannels().get(0));
      ConfigDescription configDescriptor = createChannelConfigDescriptor(configUID);
      configDescriptions.put(configDescriptor.getUID(), configDescriptor);

      ThingTypeUID uid = entry.getThingTypeUID();
      List<ChannelDefinition> channels = entry.getChannels().stream()
        .map(this::lookupChannelType)
        .filter(Objects::nonNull)
        .map(TAThingTypeProvider::createChannelDefinition)
        .collect(Collectors.toList());

      ThingTypeBuilder typeBuilder = ThingTypeBuilder.instance(uid, entry.getLabel())
        .withConfigDescriptionURI(configDescriptor.getUID())
        .withChannelDefinitions(channels)
        .withSupportedBridgeTypeUIDs(Collections.singletonList(TA_DEVICE_THING_TYPE.getAsString()));

      thingTypes.put(uid, typeBuilder.build());
    }
  }

  private ChannelType lookupChannelType(ChannelTypeUID type) {
    ChannelType channelType = channelTypeProvider.getChannelType(type, null);
    if (channelType == null) {
      logger.info("Unknown channel type {} referred by thing type {}", type, type.getId());
    }
    return channelType;
  }

  private ConfigDescription createChannelConfigDescriptor(TAChannelTypeProvider.ConfigUID configUID) {
    ConfigDescriptionBuilder builder = ConfigDescriptionBuilder.create(URI.create(configUID.getAsString()));

    ConfigDescriptionParameter readObjectIndex = ConfigDescriptionParameterBuilder.create("readObjectIndex", Type.INTEGER)
      .withLabel("Read object index")
      .withDescription("Index of CAN output used by controller to report values.")
      .build();
    ConfigDescriptionParameter writeObjectIndex = ConfigDescriptionParameterBuilder.create("writeObjectIndex", Type.INTEGER)
      .withLabel("Write object index")
      .withDescription("Index of CAN input used by controller to receive values.")
      .build();

    return builder.withParameter(readObjectIndex).withParameter(writeObjectIndex).build();
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
    return configDescriptions.values();
  }

  @Override
  public ConfigDescription getConfigDescription(URI uri, Locale locale) {
    return configDescriptions.get(uri);
  }

  static class ConfigUID extends UID {

    public ConfigUID(String dimension) {
      super(BINDING_ID, "ta-thing-" + dimension);
    }

    @Override
    protected int getMinimalNumberOfSegments() {
      return 2;
    }
  }

  private static ChannelDefinition createChannelDefinition(ChannelType channelType) {
    ChannelDefinitionBuilder definitionBuilder = new ChannelDefinitionBuilder(channelType.getUID().getId(), channelType.getUID());
    definitionBuilder.withLabel(channelType.getLabel());
    definitionBuilder.withDescription(channelType.getDescription());
    definitionBuilder.withAutoUpdatePolicy(channelType.getAutoUpdatePolicy());
    return definitionBuilder.build();
  }

}
