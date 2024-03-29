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

import static org.simplify4u.jfatek.registers.DisReg.*;

import org.connectorio.addons.binding.fatek.config.channel.binary.DiscreteChannelConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.DisReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;

public class BinaryChannelHandler implements FatekChannelHandler {

  private final DisReg register;
  private final ChannelUID channel;
  private final boolean invert;

  public <T> BinaryChannelHandler(Channel channel, DiscreteChannelConfig config) {
    this.channel = channel.getUID();
    this.invert = config.invert;
    switch (config.getRegister()) {
      case X:
        this.register = X(config.getIndex());
        break;
      case Y:
        this.register = Y(config.getIndex());
        break;
      case M:
        this.register = M(config.getIndex());
        break;
      case S:
        this.register = S(config.getIndex());
        break;
      case T:
        this.register = T(config.getIndex());
        break;
      case C:
        this.register = C(config.getIndex());
        break;
      default:
        throw new IllegalArgumentException("Unsupported register kind " + config.getRegister());
    }
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
  public RegValue prepareWrite(Command command) {
    boolean value;
    if (command instanceof OpenClosedType) {
      value = OpenClosedType.CLOSED == command;
    } else {
      value = OnOffType.ON == command;
    }

    return RegValue.getForReg(register, !invert ? !value : value);
  }

  @Override
  public State state(RegValue value) {
    boolean status = invert ? !value.boolValue() : value.boolValue();
    return status ? OnOffType.ON : OnOffType.OFF;
  }

}
