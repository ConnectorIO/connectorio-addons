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

import java.util.Arrays;
import java.util.List;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.simplify4u.jfatek.registers.Reg;
import org.simplify4u.jfatek.registers.RegValue;

public class ColorConverter {

  private final Reg color1;
  private final Reg color2;
  private final Reg color3;
  private final boolean rgb;

  public ColorConverter(Reg color1, Reg color2, Reg color3, boolean rgb) {
    this.color1 = color1;
    this.color2 = color2;
    this.color3 = color3;
    this.rgb = rgb;
  }

  public List<RegValue> toValue(HSBType command) {
    if (rgb) {
      PercentType[] rgb = command.toRGB();
      return Arrays.asList(
        RegValue.getForReg(color1, rgb[0].toBigDecimal().intValue()),
        RegValue.getForReg(color2, rgb[1].toBigDecimal().intValue()),
        RegValue.getForReg(color3, rgb[2].toBigDecimal().intValue())
      );
    }
    return Arrays.asList(
      RegValue.getForReg(color1, command.getHue().intValue()),
      RegValue.getForReg(color2, command.getSaturation().intValue()),
      RegValue.getForReg(color3, command.getBrightness().intValue())
    );
  }

  public State toState(RegValue value1, RegValue value2, RegValue value3) {
    if (rgb) {
      return HSBType.fromRGB(
        Math.min(value1.intValueUnsigned(), 255),
        Math.min(value2.intValueUnsigned(), 255),
        Math.min(value3.intValueUnsigned(), 255)
      );
    }
    return new HSBType(
      new DecimalType(Math.min(value1.intValueUnsigned(), 360)),
      new PercentType(Math.min(value2.intValueUnsigned(), 100)),
      new PercentType(Math.min(value3.intValueUnsigned(), 100))
    );
  }
}
