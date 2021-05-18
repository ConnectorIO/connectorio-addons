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
package org.connectorio.addons.binding.plc4x.canopen.internal.statistics;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.measure.Quantity;
import org.connectorio.addons.binding.can.statistic.CANStatisticCollector;
import org.openhab.core.library.dimension.DataAmount;
import org.openhab.core.library.dimension.DataTransferRate;
import org.openhab.core.library.unit.Units;
import tec.uom.se.quantity.Quantities;

/**
 * Bit ratio calculation.
 *
 * Takes in byte statistic and returns amount of bits exchanged between sampling periods.
 */
class BandwidthStatisticCollector extends SysfsReader<DataAmount> implements CANStatisticCollector<DataTransferRate> {

  private final Supplier<Long> clock;
  private Long timestamp;
  private Quantity<DataAmount> value;

  public BandwidthStatisticCollector(Supplier<Long> clock, String iface, String name) {
    super(iface, name, Units.BYTE);
    this.clock = clock;
  }

  @Override
  public String getName() {
    return super.getName() + "_bandwidth";
  }

  @Override
  public Quantity<DataTransferRate> getStatistic() {
    long time = clock.get();
    Quantity<DataAmount> count = readStatistic();

    try {
      if (timestamp == null) {
        return Quantities.getQuantity(0.0, Units.BIT_PER_SECOND);
      }

      long period = time - timestamp;
      Quantity<DataAmount> difference = count.subtract(value).to(Units.BIT);

      // twist bytes into bits and then divide by seconds between samples
      return (Quantity<DataTransferRate>) difference.divide(Quantities.getQuantity(TimeUnit.MILLISECONDS.toSeconds(period), Units.SECOND));
    } finally {
      mark(count, time);
    }
  }

  private void mark(Quantity<DataAmount> count, long time) {
    value = count;
    timestamp = time;
  }

}
