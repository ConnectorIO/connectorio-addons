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

import org.connectorio.addons.binding.fatek.internal.channel.converter.Converter;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekPLC;
import org.simplify4u.jfatek.FatekWriteDataCmd;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValueData;

public class DataChannelHandler implements FatekChannelHandler {

  private final ChannelUID channel;
  private final DataReg register;
  private final Converter converter;

  public DataChannelHandler(Channel channel, DataReg register, Converter converter) {
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
    if (value instanceof RegValueData) {
      return new FatekWriteDataCmd((FatekPLC) null, register, (RegValueData) value);
    }
    return null;
  }

  @Override
  public State state(RegValue value) {
    return converter.toState(value);
  }

}
