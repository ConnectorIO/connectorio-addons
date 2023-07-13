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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.canopen.internal.CANopenBindingConstants;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;

@Component(property = {"canopen=true", "sdo=true"})
public class CoSdoChannelTypeProvider extends BaseChannelTypeProvider implements ChannelTypeProvider, ConfigDescriptionProvider {

  private final Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<>();
  private final ConfigDescription configDescription;

  public CoSdoChannelTypeProvider() {
    URI configUri = URI.create(CANopenBindingConstants.BINDING_ID + ":sdo");
    configDescription = createConfigDescription(configUri);

    for (ChannelTypeDef definition : ENTRIES) {
      CANOpenDataType type = definition.getType();

      ChannelType channel = createChannelType(definition, type, configUri);
      channelTypes.put(channel.getUID(), channel);
    }
  }

  @Override
  public Collection<ChannelType> getChannelTypes(Locale locale) {
    return channelTypes.values();
  }

  @Override
  public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
    return channelTypes.get(channelTypeUID);
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

  private ChannelType createChannelType(ChannelTypeDef definition, CANOpenDataType type, URI configUri) {
    ChannelTypeUID channelTypeUID = createChannelTypeUID(type);

    StateDescriptionFragment stateDescription = StateDescriptionFragmentBuilder.create()
      .withPattern(definition.getPattern()).build();

    StateChannelTypeBuilder channel = ChannelTypeBuilder.state(channelTypeUID, createLabel(type), definition.getItemType())
      .withDescription("Mapping of CANopen type " + type.name() + " which takes " + type.getNumBits() + " bit" + (type.getNumBits() > 1 ? "s" : ""));
    channel.withStateDescriptionFragment(stateDescription);
    channel.withConfigDescriptionURI(configUri);
    return channel.build();
  }

  private ConfigDescription createConfigDescription(URI uri) {
    ConfigDescriptionBuilder configBuilder = ConfigDescriptionBuilder.create(uri);
    configBuilder.withParameter(ConfigDescriptionParameterBuilder.create("index", Type.INTEGER)
      .withMinimum(BigDecimal.ZERO)
      .withMaximum(BigDecimal.valueOf(0xFFFF))
      .withLabel("Index")
      .withDescription("Index is first coordinate used to request data. Permitted values start from 0 and end at 65535 (0xFFFF).")
      .withRequired(true)
      .build());
    configBuilder.withParameter(ConfigDescriptionParameterBuilder.create("subIndex", Type.INTEGER)
      .withMinimum(BigDecimal.ZERO)
      .withMaximum(BigDecimal.valueOf(0xFF))
      .withLabel("Subindex")
      .withDescription("Subindex is second coordinate used to request data. Its value are starting from 0 up to 255 (0xFF).")
      .withRequired(true)
      .build());
    configBuilder.withParameter(ConfigDescriptionParameterBuilder.create("refreshInterval", Type.INTEGER)
      .withLabel("Refresh interval")
      .withDescription("Time between next poll cycles. If no value is given polling is based on value set on thing or bridge value.")
      .withRequired(true)
      .withDefault("60000")
      .withUnitLabel("ms")
      .build());

    return configBuilder.build();
  }

  public static ChannelTypeUID createChannelTypeUID(CANOpenDataType type) {
    return new ChannelTypeUID(CANopenBindingConstants.BINDING_ID, "canopen-sdo-" + type.name().toLowerCase());
  }

}
