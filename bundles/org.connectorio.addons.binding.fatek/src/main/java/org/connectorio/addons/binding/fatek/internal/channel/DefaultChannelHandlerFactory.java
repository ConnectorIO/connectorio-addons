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
import org.connectorio.addons.binding.fatek.config.channel.color.ColorChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.data.Data32ChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.data.DataChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.percent.Percent32ChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.percent.PercentChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.rollershutter.RollerShutter32ChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.rollershutter.RollerShutterChannelConfig;
import org.connectorio.addons.binding.fatek.internal.RegisterParser;
import org.connectorio.addons.binding.fatek.internal.channel.converter.ColorConverter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.ContactConverter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Converter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Data16Converter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Data32Converter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.DiscreteConverter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Percent16Converter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Percent32Converter;
import org.openhab.core.thing.Channel;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.DisReg;

public class DefaultChannelHandlerFactory implements FatekChannelHandlerFactory {

  @Override
  public FatekChannelHandler create(Channel channel) {
    if (FatekBindingConstants.CHANNEL_TYPE_CONTACT.equals(channel.getChannelTypeUID())) {
      DiscreteChannelConfig config = channel.getConfiguration().as(DiscreteChannelConfig.class);
      return new BinaryChannelHandler(channel, RegisterParser.parseDiscrete(config), new ContactConverter(config));
    } else if (FatekBindingConstants.CHANNEL_TYPE_DISCRETE.equals(channel.getChannelTypeUID())) {
      DiscreteChannelConfig config = channel.getConfiguration().as(DiscreteChannelConfig.class);
      return new BinaryChannelHandler(channel, RegisterParser.parseDiscrete(config), new DiscreteConverter(config));
    }
    if (FatekBindingConstants.CHANNEL_TYPE_DATA16.equals(channel.getChannelTypeUID())) {
      DataChannelConfig config = channel.getConfiguration().as(DataChannelConfig.class);
      return new DataChannelHandler(channel, RegisterParser.parseData16(config), new Data16Converter(config));
    }
    if (FatekBindingConstants.CHANNEL_TYPE_DATA32.equals(channel.getChannelTypeUID())) {
      Data32ChannelConfig config = channel.getConfiguration().as(Data32ChannelConfig.class);
      return new DataChannelHandler(channel, RegisterParser.parseData32(config), new Data32Converter(config));
    }
    if (FatekBindingConstants.CHANNEL_TYPE_PERCENT16.equals(channel.getChannelTypeUID())) {
      PercentChannelConfig config = channel.getConfiguration().as(PercentChannelConfig.class);
      return new PercentChannelHandler(channel, config.step, RegisterParser.parseData16(config),
          new Percent16Converter(config)
      );
    }
    if (FatekBindingConstants.CHANNEL_TYPE_PERCENT32.equals(channel.getChannelTypeUID())) {
      Percent32ChannelConfig config = channel.getConfiguration().as(Percent32ChannelConfig.class);
      return new PercentChannelHandler(channel, config.step, RegisterParser.parseData16(config),
        new Percent32Converter(config)
      );
    }
    if (FatekBindingConstants.CHANNEL_TYPE_ROLLERSHUTTER16.equals(channel.getChannelTypeUID())) {
      RollerShutterChannelConfig config = channel.getConfiguration().as(RollerShutterChannelConfig.class);
      DiscreteChannelConfig startCfg = new DiscreteChannelConfig(config.startRegister, config.startIndex, config.startInvert);
      DiscreteChannelConfig stopCfg = new DiscreteChannelConfig(config.stopRegister, config.stopIndex, config.stopInvert);
      return new RollerShutterChannelHandler(channel, RegisterParser.parseData16(config),
        RegisterParser.parseDiscrete(startCfg),
        RegisterParser.parseDiscrete(startCfg),
        new Data16Converter(config),
        new DiscreteConverter(startCfg),
        new DiscreteConverter(stopCfg)
      );
    }
    if (FatekBindingConstants.CHANNEL_TYPE_ROLLERSHUTTER32.equals(channel.getChannelTypeUID())) {
      RollerShutter32ChannelConfig config = channel.getConfiguration().as(RollerShutter32ChannelConfig.class);
      DiscreteChannelConfig startCfg = new DiscreteChannelConfig(config.startRegister, config.startIndex, config.startInvert);
      DiscreteChannelConfig stopCfg = new DiscreteChannelConfig(config.stopRegister, config.stopIndex, config.stopInvert);
      return new RollerShutterChannelHandler(channel, RegisterParser.parseData16(config),
        RegisterParser.parseDiscrete(startCfg),
        RegisterParser.parseDiscrete(startCfg),
        new Data32Converter(config),
        new DiscreteConverter(startCfg),
        new DiscreteConverter(stopCfg)
      );
    }
    if (FatekBindingConstants.CHANNEL_TYPE_COLOR16.equals(channel.getChannelTypeUID())) {
      ColorChannelConfig config = channel.getConfiguration().as(ColorChannelConfig.class);
      DataReg color1cfg = RegisterParser.parseData16(new DataChannelConfig(config.color1register, config.color1index));
      DataReg color2cfg = RegisterParser.parseData16(new DataChannelConfig(config.color2register, config.color2index));
      DataReg color3cfg = RegisterParser.parseData16(new DataChannelConfig(config.color3register, config.color3index));
      return createColorHandler(channel, config, color1cfg, color2cfg, color3cfg);
    }
    if (FatekBindingConstants.CHANNEL_TYPE_COLOR32.equals(channel.getChannelTypeUID())) {
      ColorChannelConfig config = channel.getConfiguration().as(ColorChannelConfig.class);
      DataReg color1cfg = RegisterParser.parseData32(new DataChannelConfig(config.color1register, config.color1index));
      DataReg color2cfg = RegisterParser.parseData32(new DataChannelConfig(config.color2register, config.color2index));
      DataReg color3cfg = RegisterParser.parseData32(new DataChannelConfig(config.color3register, config.color3index));
      return createColorHandler(channel, config, color1cfg, color2cfg, color3cfg);
    }
    return null;
  }

  private FatekChannelHandler createColorHandler(Channel channel, ColorChannelConfig config, DataReg color1, DataReg color2, DataReg color3) {
    DisReg switcher = null;
    Converter switcherConverter = null;
    if (config.switcherRegister != null && config.switcherIndex != 0) {
      DiscreteChannelConfig switchCfg = new DiscreteChannelConfig(config.switcherRegister, config.switcherIndex, config.switcherInvert);
      switcher = RegisterParser.parseDiscrete(switchCfg);
      switcherConverter = new DiscreteConverter(switchCfg);
    }
    return new ColorChannelHandler(channel, config,
      color1, color2, color3,
      new ColorConverter(color1, color2, color3, config.rgb),
      switcher, switcherConverter
    );
  }

}
