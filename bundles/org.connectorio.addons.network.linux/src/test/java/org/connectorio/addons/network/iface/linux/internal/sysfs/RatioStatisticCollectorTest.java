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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.measure.Quantity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tec.uom.se.AbstractUnit;
import tec.uom.se.quantity.Quantities;

@ExtendWith(MockitoExtension.class)
class RatioStatisticCollectorTest {

  public static final String BASE_STATISTIC = "tx_packets";
  @Mock
  Supplier<Long> clock;

  @Test
  void testRatioCalculation() {
    Map<String, Quantity<?>> statistics = new HashMap<>();
    RatioStatisticProcessor collector = new RatioStatisticProcessor(BASE_STATISTIC);

    when(clock.get()).thenReturn(0L);
    statistics.put(BASE_STATISTIC, Quantities.getQuantity(0, AbstractUnit.ONE));

    Quantity<?> collectorStatistic = collector.process(clock, statistics);
    assertThat(collectorStatistic)
      .extracting(Quantity::getValue)
      .extracting(Number::doubleValue)
      .isEqualTo(0.0);

    when(clock.get()).thenReturn(1000L);
    statistics.put(BASE_STATISTIC, Quantities.getQuantity(1L, AbstractUnit.ONE));
    collectorStatistic = collector.process(clock, statistics);
    assertThat(collectorStatistic)
      .extracting(Quantity::getValue)
      .extracting(Number::doubleValue)
      .isEqualTo(1.0);
  }

}
