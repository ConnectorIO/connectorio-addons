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
package org.connectorio.addons.binding.fatek.internal.channel;

import org.connectorio.addons.binding.fatek.FatekBindingConstants;
import org.connectorio.addons.binding.fatek.config.channel.binary.DiscreteChannelConfig;
import org.openhab.core.thing.Channel;

public class DefaultChannelHandlerFactory implements FatekChannelHandlerFactory {

  @Override
  public FatekChannelHandler create(Channel channel) {
    if (FatekBindingConstants.CHANNEL_TYPE_DISCRETE_INPUT.equals(channel.getChannelTypeUID())) {
      DiscreteChannelConfig config = channel.getConfiguration().as(DiscreteChannelConfig.class);
      return new BinaryChannelHandler(true, channel, config);
    }
    if (FatekBindingConstants.CHANNEL_TYPE_DISCRETE_OUTPUT.equals(channel.getChannelTypeUID())) {
      DiscreteChannelConfig config = channel.getConfiguration().as(DiscreteChannelConfig.class);
      return new BinaryChannelHandler(false, channel, config);
    }
    return null;
  }

}