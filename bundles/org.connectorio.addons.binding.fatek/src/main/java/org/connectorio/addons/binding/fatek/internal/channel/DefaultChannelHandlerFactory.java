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

import java.math.BigInteger;
import org.connectorio.addons.binding.fatek.FatekBindingConstants;
import org.connectorio.addons.binding.fatek.config.channel.data.Data32ChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.data.DataChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.binary.DiscreteChannelConfig;
import org.openhab.core.thing.Channel;
import org.simplify4u.jfatek.registers.RegValue16;
import org.simplify4u.jfatek.registers.RegValue32;

public class DefaultChannelHandlerFactory implements FatekChannelHandlerFactory {

  @Override
  public FatekChannelHandler create(Channel channel) {
    if (FatekBindingConstants.CHANNEL_TYPE_DISCRETE.equals(channel.getChannelTypeUID())) {
      DiscreteChannelConfig config = channel.getConfiguration().as(DiscreteChannelConfig.class);
      return new BinaryChannelHandler(channel, config);
    }
    if (FatekBindingConstants.CHANNEL_TYPE_DATA16.equals(channel.getChannelTypeUID())) {
      DataChannelConfig config = channel.getConfiguration().as(DataChannelConfig.class);
      return new DataChannelHandler(channel, config, (register, state) -> {
        int intValue = state.toBigDecimal().intValue();
        return RegValue16.getForReg(register, config.unsigned ? Integer.toUnsignedLong(intValue) : intValue);
      });
    }
    if (FatekBindingConstants.CHANNEL_TYPE_DATA32.equals(channel.getChannelTypeUID())) {
      Data32ChannelConfig config = channel.getConfiguration().as(Data32ChannelConfig.class);
      return new DataChannelHandler(channel, config, (register, state) -> {
        if (config.floating) {
          return RegValue32.getForReg(register, state.toBigDecimal().floatValue());
        }

        long longValue = state.toBigDecimal().longValue();
        return RegValue32.getForReg(register, config.unsigned ? BigInteger.valueOf(longValue).longValue() : longValue);
      });
    }
    return null;
  }

}
