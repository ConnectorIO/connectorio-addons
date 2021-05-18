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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.measure.Quantity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.dimension.DataAmount;
import org.openhab.core.library.dimension.DataTransferRate;
import org.openhab.core.library.unit.Units;
import tec.uom.se.quantity.Quantities;

@ExtendWith(MockitoExtension.class)
class BandwidthStatisticCollectorTest {

  @Mock
  Supplier<Long> clock;
  AtomicReference<Quantity<DataAmount>> statistic = new AtomicReference();

  @Test
  void testRatioCalculation() {
    BandwidthStatisticCollector collector = new BandwidthStatisticCollector(clock, "foo", "tx_bytes") {
      @Override
      protected Quantity<DataAmount> readStatistic() {
        return statistic.get();
      }
    };

    when(clock.get()).thenReturn(1000L);
    statistic.set(Quantities.getQuantity(0, Units.BYTE));
    assertThat(collector.getStatistic())
      .extracting(Quantity::getValue)
      .isEqualTo(0.0);

    // one second and one byte later we expect 8 bit/s ratio.
    when(clock.get()).thenReturn(2000L);
    statistic.set(Quantities.getQuantity(1, Units.BYTE));
    Quantity<DataTransferRate> statistic = collector.getStatistic();
    assertThat(statistic)
      .extracting(Quantity::getValue)
      .extracting(Number::doubleValue) // escape from BigDecimal
      .isEqualTo(8.0);

    // eight seconds and one byte later we expect 1 bit/s ratio.
    when(clock.get()).thenReturn(10000L);
    this.statistic.set(Quantities.getQuantity(2, Units.BYTE));
    assertThat(collector.getStatistic())
      .extracting(Quantity::getValue)
      .extracting(Number::doubleValue)
      .isEqualTo(1.0);
  }
}