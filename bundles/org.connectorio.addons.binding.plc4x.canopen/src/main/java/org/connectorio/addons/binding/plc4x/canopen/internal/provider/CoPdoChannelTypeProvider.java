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
package org.connectorio.addons.binding.plc4x.canopen.internal.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.plc4x.canopen.internal.CANopenBindingConstants;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;

@Component(property = {"canopen=true", "pdo=true"})
public class CoPdoChannelTypeProvider extends BaseChannelTypeProvider implements ChannelTypeProvider {

  private final Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<>();

  public CoPdoChannelTypeProvider() {
    for (ChannelTypeDef definition : ENTRIES) {
      CANOpenDataType type = definition.getType();

      ChannelType channel = createChannelType(true, definition, type);
      channelTypes.put(channel.getUID(), channel);
      channel = createChannelType(false, definition, type);
      channelTypes.put(channel.getUID(), channel);
    }
  }

  private ChannelType createChannelType(boolean rpdo, ChannelTypeDef definition, CANOpenDataType type) {
    ChannelTypeUID channelTypeUID = createChannelTypeUID(rpdo, type);

    StateDescriptionFragment stateDescription = StateDescriptionFragmentBuilder.create()
      .withPattern(definition.getPattern())
      .withReadOnly(rpdo)
      .build();

    StateChannelTypeBuilder channel = ChannelTypeBuilder.state(channelTypeUID, createLabel(type), definition.getItemType())
      .withDescription("Mapping of CANopen type " + type.name() + " which takes " + type.getNumBits() + " bit" + (type.getNumBits() > 1 ? "s" : ""));
    channel.withStateDescriptionFragment(stateDescription);
    return channel.build();
  }

  @Override
  public Collection<ChannelType> getChannelTypes(Locale locale) {
    return channelTypes.values();
  }

  @Override
  public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
    return channelTypes.get(channelTypeUID);
  }

  static ChannelTypeUID createChannelTypeUID(boolean rpdo, CANOpenDataType type) {
    if (rpdo) {
      return new ChannelTypeUID(CANopenBindingConstants.BINDING_ID, "canopen-rpdo-" + type.name().toLowerCase());
    }
    return new ChannelTypeUID(CANopenBindingConstants.BINDING_ID, "canopen-tpdo-" + type.name().toLowerCase());
  }

}
