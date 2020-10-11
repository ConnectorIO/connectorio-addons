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
package org.connectorio.binding.transformation.inverse.profile;

import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.profiles.ProfileAdvisor;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class ConnectorioProfileAdvisor implements ProfileAdvisor {

  @Override
  public ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, String itemType) {
    if (channel.getKind() == ChannelKind.STATE && CoreItemFactory.SWITCH.equalsIgnoreCase(itemType)) {
      return ConnectorioProfiles.TOGGLE_SWITCH_STATE;
    }
    return null;
  }

  @Override
  public ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, String itemType) {
    if (channelType.getKind() == ChannelKind.STATE && CoreItemFactory.SWITCH.equalsIgnoreCase(itemType)) {
      return ConnectorioProfiles.TOGGLE_SWITCH_STATE;
    }
    return null;
  }
}
