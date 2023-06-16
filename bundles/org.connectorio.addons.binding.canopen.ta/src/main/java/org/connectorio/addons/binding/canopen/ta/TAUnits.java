/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.canopen.ta;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.AbstractConverter;
import tech.units.indriya.unit.TransformedUnit;

public class TAUnits {

  public static final Unit<Dimensionless> IMPULSE = new TransformedUnit<>(AbstractUnit.ONE, AbstractConverter.IDENTITY);
  public static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(SIUnits.METRE);

  static {
    SimpleUnitFormat.getInstance().label(IMPULSE, "Impl");
  }

}
