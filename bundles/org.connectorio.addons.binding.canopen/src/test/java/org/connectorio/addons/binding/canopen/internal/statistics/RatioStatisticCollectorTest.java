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
package org.connectorio.addons.binding.canopen.internal.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;

@ExtendWith(MockitoExtension.class)
class RatioStatisticCollectorTest {

  @Mock
  Supplier<Long> clock;
  AtomicReference<Quantity<Dimensionless>> statistic = new AtomicReference<>();

  @Test
  void testRatioCalculation() {
    RatioStatisticCollector collector = new RatioStatisticCollector(clock, "foo", "tx_packets") {
      @Override
      protected Quantity<Dimensionless> readStatistic() {
        return statistic.get();
      }
    };

    when(clock.get()).thenReturn(1000L);
    statistic.set(Quantities.getQuantity(1000, AbstractUnit.ONE));
    Quantity<?> collectorStatistic = collector.getStatistic();
    assertThat(collectorStatistic)
      .extracting(Quantity::getValue)
      .extracting(Number::doubleValue)
      .isEqualTo(0.0);

    when(clock.get()).thenReturn(2000L);
    this.statistic.set(Quantities.getQuantity(1001, AbstractUnit.ONE));
    collectorStatistic = collector.getStatistic();
    assertThat(collectorStatistic)
      .extracting(Quantity::getValue)
      .extracting(Number::doubleValue)
      .isEqualTo(1.0);
  }

}
