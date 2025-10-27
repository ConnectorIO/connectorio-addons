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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Converter;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekWriteDataCmd;
import org.simplify4u.jfatek.FatekWriteMixDataCmd;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.DisReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValueData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollerShutterChannelHandler implements FatekChannelHandler {

  private final Logger logger = LoggerFactory.getLogger(RollerShutterChannelHandler.class);

  private final ChannelUID channel;
  private final DataReg register;
  private final DisReg upReg;
  private final DisReg downReg;
  private final Converter positionConverter;
  private final Converter upConverter;
  private final Converter downConverter;

  public RollerShutterChannelHandler(Channel channel, DataReg register, DisReg upReg, DisReg downReg,
    Converter positionConverter, Converter upConverter, Converter downConverter) {
    this.channel = channel.getUID();
    this.register = register;
    this.upReg = upReg;
    this.downReg = downReg;
    this.positionConverter = positionConverter;
    this.upConverter = upConverter;
    this.downConverter = downConverter;
  }

  @Override
  public List<Reg> registers() {
    return Collections.singletonList(register);
  }

  @Override
  public ChannelUID channel() {
    return channel;
  }

  @Override
  public FatekCommand<?> prepareWrite(Command command) {
    RegValue positionValue = positionConverter.toValue(command);
    if (positionValue instanceof RegValueData) {
      return new FatekWriteDataCmd(null, register, (RegValueData) positionValue);
    }

    if (UpDownType.UP.equals(command)) {
      RegValue start = upConverter.toValue(OnOffType.ON);
      return new FatekWriteMixDataCmd(null, Map.of(
        upReg, start
      ));
    } else if (UpDownType.DOWN.equals(command)) {
      RegValue stop = downConverter.toValue(OnOffType.ON);
      return new FatekWriteMixDataCmd(null, Map.of(
        downReg, stop
      ));
    } else if (StopMoveType.STOP.equals(command)) {
      if (upReg.equals(downReg)) {
        return new FatekWriteMixDataCmd(null, Map.of(
          upReg, upConverter.toValue(OnOffType.OFF)
        ));
      }
      RegValue start = upConverter.toValue(OnOffType.OFF);
      RegValue stop = downConverter.toValue(OnOffType.OFF);
      return new FatekWriteMixDataCmd(null, Map.of(
        upReg, start,
        downReg, stop
      ));
    }

    logger.warn("Unsupported command {} received by rollershutter channel {}", command, channel);

    return null;
  }

  @Override
  public State state(List<RegValue> value) {
    return positionConverter.toState(value.get(0));
  }

  @Override
  public String validateConfiguration() {
    return null;
  }

}
