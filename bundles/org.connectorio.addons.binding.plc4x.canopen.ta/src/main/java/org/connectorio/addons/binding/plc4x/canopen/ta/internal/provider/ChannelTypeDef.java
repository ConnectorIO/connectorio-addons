/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.connectorio.addons.binding.plc4x.canopen.CANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

public class ChannelTypeDef {

  private final ThingTypeUID thingType;
  private final ChannelTypeUID channelType;
  private final String itemType;
  private final String label;
  private final List<TAUnit> units;

  ChannelTypeDef(ThingTypeUID thingType, String itemType, TAUnit ... units) {
    this(thingType, new ChannelTypeUID(TACANopenBindingConstants.BINDING_ID, thingType.getId()),
      itemType, label(thingType.getId()), units);
  }

  ChannelTypeDef(ThingTypeUID thingType, ChannelTypeUID channelType, String itemType, String label, TAUnit ... units) {
    this.thingType = thingType;
    this.channelType = channelType;
    this.itemType = itemType;
    this.label = label;
    this.units = Arrays.asList(units);
  }

  public ThingTypeUID getThingType() {
    return thingType;
  }

  public ChannelTypeUID getChannelType() {
    return channelType;
  }

  public String getItemType() {
    return itemType;
  }

  public String toString() {
    return "TypeEntry [channelType=" + channelType + ", itemType=" + itemType + "]";
  }

  public String getLabel() {
    return label;
  }

  public List<TAUnit> getUnits() {
    return units;
  }

  public StateDescriptionFragment getStateDescriptionFragment() {
    return StateDescriptionFragmentBuilder.create().withPattern("%.1f %unit%").build();
  }

  private static String label(String dimension) {
    String label = dimension.replace(TACANopenBindingConstants.TA_ANALOG_PREFIX + "-", "")
      .replace(TACANopenBindingConstants.TA_DIGITAL_PREFIX + "-", "")
      .replace("-", " ").toLowerCase(Locale.ROOT);
    return label.substring(0, 1).toUpperCase() + label.substring(1);
  }

}
