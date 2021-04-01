/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.canopen.internal.statistics;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import tec.uom.se.AbstractUnit;
import tec.uom.se.quantity.Quantities;

/**
 * Ratio calculation.
 *
 * Calculates number of exchanges between sampling periods.
 */
class RatioStatisticCollector extends SysfsStatisticCollector<Dimensionless> {

  private final Supplier<Long> clock;
  private Long timestamp;
  private Quantity<Dimensionless> value;

  public RatioStatisticCollector(Supplier<Long> clock, String iface, String name) {
    super(iface, name, AbstractUnit.ONE);
    this.clock = clock;
  }

  @Override
  public String getName() {
    return super.getName() + "_ratio";
  }

  @Override
  public Quantity<Dimensionless> getStatistic() {
    long time = clock.get();
    Quantity<Dimensionless> count = readStatistic();

    try {
      if (timestamp == null) {
        return Quantities.getQuantity(0.0, AbstractUnit.ONE);
      }

      long period = time - timestamp;
      Quantity<Dimensionless> difference = count.subtract(value);

      // twist bytes into bits and then divide by seconds between samples
      double ratio = difference.getValue().doubleValue() / (int) TimeUnit.MILLISECONDS.toSeconds(period);
      return Quantities.getQuantity(ratio, AbstractUnit.ONE);
    } finally {
      mark(count, time);
    }
  }

  private void mark(Quantity<Dimensionless> count, long time) {
    value = count;
    timestamp = time;
  }

}
