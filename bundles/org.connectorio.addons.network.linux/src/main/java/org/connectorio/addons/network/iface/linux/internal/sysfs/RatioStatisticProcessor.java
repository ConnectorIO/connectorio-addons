/*
 * Copyright (C) 2019-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.network.iface.linux.internal.sysfs;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.measure.IncommensurableException;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import org.connectorio.addons.network.iface.linux.internal.StatisticProcessor;
import tec.uom.se.AbstractUnit;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

/**
 * Ratio calculation - calculates number of exchanges within sampling periods.
 */
@SuppressWarnings("rawtypes")
class RatioStatisticProcessor implements StatisticProcessor {

  private final String baseStatistic;
  private Long timestamp;
  private Quantity value;

  public RatioStatisticProcessor(String baseStatistic) {
    this.baseStatistic = baseStatistic;
  }

  @Override
  public Quantity<?> process(Supplier<Long> clock, Map<String, Quantity<?>> stats) {
    long time = clock.get();
    Quantity<?> quantity = stats.get(baseStatistic);

    if (quantity == null) {
      return null;
    }

    Quantity<?> count = null;
    try {
      count = quantity;
      Unit<?> targetUnit = quantity.getUnit().divide(Units.SECOND);

      if (timestamp == null || value == null) {
        return Quantities.getQuantity(0.0, targetUnit);
      }

      long period = time - timestamp;
      Quantity<?> difference = count.subtract(value);

      // twist value into increased value and then divide by seconds between samples
      double ratio = difference.getValue().doubleValue() / TimeUnit.MILLISECONDS.toSeconds(period);
      return Quantities.getQuantity(ratio, targetUnit);
    } finally {
      mark(count, time);
    }
  }

  private void mark(Quantity<?> count, long time) {
    value = count;
    timestamp = time;
  }

}
