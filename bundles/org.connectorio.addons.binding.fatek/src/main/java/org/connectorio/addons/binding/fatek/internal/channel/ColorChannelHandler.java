/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.connectorio.addons.binding.fatek.config.channel.color.ColorChannelConfig;
import org.connectorio.addons.binding.fatek.internal.channel.converter.ColorConverter;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Converter;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekWriteDiscreteCmd;
import org.simplify4u.jfatek.FatekWriteMixDataCmd;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.DisReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorChannelHandler implements FatekChannelHandler {

  private final Logger logger = LoggerFactory.getLogger(ColorChannelHandler.class);

  private final ChannelUID channel;
  private final ColorChannelConfig config;
  private final DataReg color1;
  private final DataReg color2;
  private final DataReg color3;
  private final DisReg switcher;
  private final ColorConverter converter;
  private final Converter switcherConverter;

  // a volatile state
  private AtomicReference<HSBType> state = new AtomicReference<>();

  public ColorChannelHandler(Channel channel, ColorChannelConfig config, DataReg color1, DataReg color2, DataReg color3,
    ColorConverter converter, DisReg switcher, Converter switcherConverter) {
    this.channel = channel.getUID();
    this.config = config;
    this.color1 = color1;
    this.color2 = color2;
    this.color3 = color3;
    this.switcher = switcher;
    this.converter = converter;
    this.switcherConverter = switcherConverter;
  }

  @Override
  public List<Reg> registers() {
    return Arrays.asList(color1, color2, color3);
  }

  @Override
  public ChannelUID channel() {
    return channel;
  }

  @Override
  public FatekCommand<?> prepareWrite(Command command) {
    if (command instanceof IncreaseDecreaseType) {
      if (state.get() == null) {
        return null;
      }

      HSBType hsb = state.get();
      if (IncreaseDecreaseType.INCREASE.equals(command)) {
        int brightness = Math.min(hsb.getBrightness().intValue() + config.step, 100);
        return colorCommand(new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(brightness)));
      } else if (IncreaseDecreaseType.DECREASE.equals(command)) {
        int brightness = Math.max(hsb.getBrightness().intValue() - config.step, 0);
        return colorCommand(new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(brightness)));
      } else {
        return null;
      }
    } else if (command instanceof OnOffType) {
      RegValue value = switcherConverter.toValue(command);
      if (value != null) {
        return new FatekWriteDiscreteCmd(null, switcher, value.boolValue());
      }
      return null;
    } else if (command instanceof HSBType) {
      return colorCommand((HSBType) command);
    }

    logger.warn("Unsupported command {} received by color channel {}", command, channel);
    return null;
  }

  private FatekCommand<?> colorCommand(HSBType hsbType) {
    List<RegValue> value = converter.toValue(hsbType);
    if (value != null) {
      return new FatekWriteMixDataCmd(null, Map.of(
        color1, value.get(0),
        color2, value.get(1),
        color3, value.get(2)
      ));
    }
    return null;
  }

  @Override
  public State state(List<RegValue> value) {
    State convertedState = converter.toState(value.get(0), value.get(1), value.get(2));
    if (convertedState instanceof HSBType) {
      // retain last read value of color state
      state.set((HSBType) convertedState);
    }
    return convertedState;
  }


}
