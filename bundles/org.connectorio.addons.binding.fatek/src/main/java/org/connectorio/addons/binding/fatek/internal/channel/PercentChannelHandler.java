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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.connectorio.addons.binding.fatek.internal.channel.converter.Converter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekWriteDataCmd;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValueData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PercentChannelHandler implements FatekChannelHandler {

  private final Logger logger = LoggerFactory.getLogger(PercentChannelHandler.class);

  private final ChannelUID channel;
  private final BigDecimal step;
  private final DataReg register;
  private final Converter converter;

  private final AtomicReference<DecimalType> state = new AtomicReference<>();

  public PercentChannelHandler(Channel channel, BigDecimal step, DataReg register, Converter converter) {
    this.channel = channel.getUID();
    this.step = step;
    this.register = register;
    this.converter = converter;
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
    RegValue value = converter.toValue(command);
    if (value instanceof RegValueData) {
      return new FatekWriteDataCmd(null, register, (RegValueData) value);
    }

    if (command instanceof IncreaseDecreaseType) {
      if (state.get() == null) {
        logger.warn("Could not handle command {} for channel {}. State of channel is not yet known", command, channel);
        return null;
      }

      BigDecimal decimal = state.get().toBigDecimal();
      if (IncreaseDecreaseType.INCREASE.equals(command)) {
        decimal = decimal.add(step).min(Percentage.HUNDRED);
        return write(Percentage.from(decimal));
      }
      if (IncreaseDecreaseType.DECREASE.equals(command)) {
        decimal = decimal.subtract(step).max(Percentage.ZERO);
        return write(Percentage.from(decimal));
      }

    }
    return null;
  }

  private FatekWriteDataCmd write(PercentType percentage) {
    RegValue value = converter.toValue(percentage);
    if (value instanceof RegValueData) {
      return new FatekWriteDataCmd(null, register, (RegValueData) value);
    }
    return null;
  }

  @Override
  public State state(List<RegValue> value) {
    State convertedState = converter.toState(value.get(0));
    if (convertedState instanceof DecimalType) {
      state.set((DecimalType) convertedState);
    }
    return convertedState;
  }

  @Override
  public String validateConfiguration() {
    return null;
  }

}
