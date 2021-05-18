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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

public class ThingTypeDef {

  private final ThingTypeUID thingTypeUID;
  private final List<ChannelTypeUID> channels;

  public ThingTypeDef(ThingTypeUID thingType) {
    this(thingType, Collections.singletonList(new ChannelTypeUID(TACANopenBindingConstants.BINDING_ID, thingType.getId())));
  }

  public ThingTypeDef(ThingTypeUID thingType, List<ChannelTypeUID> channels) {
    this.thingTypeUID = thingType;
    this.channels = channels;
  }

  public ThingTypeUID getThingTypeUID() {
    return thingTypeUID;
  }

  public String toString() {
    return "TypeEntry [thingType=" + thingTypeUID + ", channels=" + channels + "]";
  }

  public List<ChannelTypeUID> getChannels() {
    return channels;
  }

  public String getLabel() {
    String label = thingTypeUID.getId().replace(TACANopenBindingConstants.TA_ANALOG_PREFIX + "-", "")
      .replace(TACANopenBindingConstants.TA_DIGITAL, "")
      .replace("-", " ").toLowerCase();
    return "Technische Alternative Object " + label.substring(0, 1).toUpperCase() + label.substring(1);
  }

  public String getDimension() {
    return null;
  }
}
