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
package org.connectorio.addons.binding.fatek.internal.channel.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.connectorio.addons.binding.fatek.config.channel.percent.Percent32ChannelConfig;
import org.connectorio.addons.binding.fatek.internal.channel.Percentage;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValue32;

public class Percent32Converter implements Converter {

  private final Percent32ChannelConfig config;

  public Percent32Converter(Percent32ChannelConfig config) {
    this.config = config;
  }

  @Override
  public RegValue toValue(Command command) {
    if (!(command instanceof PercentType)) {
      return null;
    }

    PercentType percentType = (PercentType) command;
    if (config.floating) {
      return new RegValue32(Float.floatToIntBits(percentType.toBigDecimal().floatValue()));
    }

    long longValue = percentType.toBigDecimal().longValue();
    return new RegValue32(config.unsigned ? BigInteger.valueOf(longValue).longValue() : longValue);
  }

  @Override
  public State toState(RegValue value) {
    if (config.floating) {
      BigDecimal decimal = BigDecimal.valueOf(value.floatValue());
      return new PercentType(decimal.max(Percentage.HUNDRED).min(Percentage.ZERO));
    }

    int number = config.unsigned ? value.intValueUnsigned() : value.intValue();
    number = Math.min(number, 100);
    number = Math.max(number, 0);
    return new PercentType(number);
  }
}
