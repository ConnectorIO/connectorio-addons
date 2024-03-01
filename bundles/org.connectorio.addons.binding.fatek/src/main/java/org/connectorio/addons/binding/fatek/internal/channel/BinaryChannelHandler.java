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

import org.connectorio.addons.binding.fatek.internal.channel.converter.Converter;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekWriteDiscreteCmd;
import org.simplify4u.jfatek.registers.DisReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;

public class BinaryChannelHandler implements FatekChannelHandler {

  private final DisReg register;
  private final Converter converter;
  private final ChannelUID channel;

  public BinaryChannelHandler(Channel channel, DisReg register, Converter converter) {
    this.channel = channel.getUID();
    this.register = register;
    this.converter = converter;
  }

  @Override
  public Reg register() {
    return register;
  }

  @Override
  public ChannelUID channel() {
    return channel;
  }

  @Override
  public FatekCommand<?> prepareWrite(Command command) {
    RegValue value = converter.toValue(command);
    if (value != null) {
      return new FatekWriteDiscreteCmd(null, register, value.boolValue());
    }
    return null;
  }

  @Override
  public State state(RegValue value) {
    return converter.toState(value);
  }

}
