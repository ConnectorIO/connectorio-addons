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

import static org.simplify4u.jfatek.registers.DataReg.*;

import java.math.BigInteger;
import java.util.function.BiFunction;
import org.connectorio.addons.binding.fatek.config.channel.data.DataChannelConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValue16;
import org.simplify4u.jfatek.registers.RegValue32;

public class DataChannelHandler implements FatekChannelHandler {

  private final DataReg register;
  private final ChannelUID channel;
  private final boolean unsigned;
  private final BiFunction<DataReg, DecimalType, RegValue> mapper;

  public DataChannelHandler(Channel channel, DataChannelConfig config, BiFunction<DataReg, DecimalType, RegValue> mapper) {
    this.channel = channel.getUID();
    this.unsigned = config.invert;
    this.mapper = mapper;
    switch (config.getRegister()) {
      case R:
        this.register = R(config.index);
        break;
      case D:
        this.register = D(config.index);
        break;
      case F:
        this.register = F(config.index);
        break;
      case DR:
        this.register = DR(config.index);
        break;
      case DD:
        this.register = DD(config.index);
        break;
      case DF:
        this.register = DF(config.index);
        break;
      case RT:
        this.register = RT(config.index);
        break;
      case RC:
        this.register = RC(config.index);
        break;
      case DRT:
        this.register = DRT(config.index);
        break;
      case DRC:
        this.register = DRC(config.index);
        break;
      case WX:
        this.register = WX(config.index);
        break;
      case WY:
        this.register = WY(config.index);
        break;
      case WM:
        this.register = WM(config.index);
        break;
      case WS:
        this.register = WS(config.index);
        break;
      case WT:
        this.register = WT(config.index);
        break;
      case WC:
        this.register = WC(config.index);
        break;
      case DWX:
        this.register = DWX(config.index);
        break;
      case DWY:
        this.register = DWY(config.index);
        break;
      case DWM:
        this.register = DWM(config.index);
        break;
      case DWS:
        this.register = DWS(config.index);
        break;
      case DWT:
        this.register = DWT(config.index);
        break;
      case DWC:
        this.register = DWC(config.index);
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
    if (!(command instanceof DecimalType)) {
      return null;
    }

    DecimalType value = (DecimalType) command;
    return mapper.apply(register, value);
  }

  @Override
  public State state(RegValue value) {
    if (register.is32Bits()) {
      return new DecimalType(value.floatValue());
    }

    return new DecimalType(unsigned ? value.longValueUnsigned() : value.longValue());
  }

}
