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
import org.connectorio.addons.network.iface.linux.internal.StatisticProcessor;
import org.openhab.core.library.dimension.DataAmount;
import org.openhab.core.library.dimension.DataTransferRate;
import org.openhab.core.library.unit.Units;
import tec.uom.se.quantity.Quantities;

/**
 * Bit ratio calculation.
 *
 * Takes in byte statistic and returns amount of bits exchanged between sampling periods.
 */
class BandwidthStatisticProcessor implements StatisticProcessor {

  private final String baseStatistic;
  private Long timestamp;
  private Quantity<DataAmount> value;

  BandwidthStatisticProcessor(String baseStatistic) {
    this.baseStatistic = baseStatistic;
  }

  @Override
  public Quantity<DataTransferRate> process(Supplier<Long> clock, Map<String, Quantity<?>> stats) {
    long time = clock.get();
    Quantity<?> quantity = stats.get(baseStatistic);

    if (quantity == null) {
      return null;
    }

    Quantity<DataAmount> count = null;
    try {
      count = Quantities.getQuantity(quantity.getUnit().getConverterToAny(Units.BIT).convert(quantity.getValue()), Units.BIT);

      if (timestamp == null || value == null) {
        return Quantities.getQuantity(0.0, Units.BIT_PER_SECOND);
      }

      long period = time - timestamp;
      Quantity<DataAmount> difference = count.subtract(value).to(Units.BIT);

      // twist bytes into bits and then divide by seconds between samples
      long timeSpan = TimeUnit.MILLISECONDS.toSeconds(period);
      if (timeSpan > 0) {
        return Quantities.getQuantity((difference.getValue().doubleValue() / timeSpan), Units.BIT_PER_SECOND);
      }
      return Quantities.getQuantity(0.0, Units.BIT_PER_SECOND);
    } catch (IncommensurableException e) {
      return null;
    } finally {
      mark(count, time);
    }
  }

  private void mark(Quantity<DataAmount> count, long time) {
    value = count;
    timestamp = time;
  }

}
