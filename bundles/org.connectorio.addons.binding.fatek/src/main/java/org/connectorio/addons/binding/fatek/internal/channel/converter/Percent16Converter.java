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
import java.math.RoundingMode;
import org.connectorio.addons.binding.fatek.config.channel.data.DataChannelConfig;
import org.connectorio.addons.binding.fatek.config.channel.percent.PercentChannelConfig;
import org.connectorio.addons.binding.fatek.internal.channel.Percentage;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.RegValue;
import org.simplify4u.jfatek.registers.RegValue16;
import org.simplify4u.jfatek.registers.RegValue32;

public class Percent16Converter implements Converter {

  private final PercentChannelConfig config;

  public Percent16Converter(PercentChannelConfig config) {
    this.config = config;
  }

  // divide
  @Override
  public RegValue toValue(Command command) {
    if (!(command instanceof PercentType)) {
      return null;
    }

    PercentType percentType = (PercentType) command;
    BigDecimal scaledValue = percentType.toBigDecimal().divide(config.factor, RoundingMode.HALF_UP);
    long longValue = scaledValue.longValue();
    return new RegValue16(config.unsigned ? BigInteger.valueOf(longValue).longValue() : longValue);
  }

  // multiply
  @Override
  public State toState(RegValue value) {
    int baseValue = config.unsigned ? value.intValueUnsigned() : value.intValue();
    BigDecimal scaledValue = BigDecimal.valueOf(baseValue).multiply(config.factor);
    return Percentage.from(scaledValue);
  }
}
