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
package org.connectorio.addons.compute.cycle.internal.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimeCalculatorTest {

  public static final int YEAR = 2020;
  public static final int MONTH = 3;
  public static final int DAY_OF_MONTH = 1;
  public static final int HOUR = 13;
  public static final int MINUTE = 30;
  public static final int SECOND = 23;
  public static final int NANO = 100;

  public static final LocalDateTime TEST_DATE = LocalDateTime.of(YEAR, MONTH, DAY_OF_MONTH, HOUR, MINUTE, SECOND, NANO);

  ZoneId zone = ZoneId.systemDefault();

  @Mock
  Supplier<Long> clock;

  @Test
  void calculateNextSecondReset() {
    TimeCalculator calculator = new TimeCalculator();

    when(clock.get()).thenReturn(TEST_DATE.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

    Long time = calculator.calculateNextReset(clock, 1L, TimeUnit.SECONDS);

    assertThat(time).isNotNull()
      .extracting(Instant::ofEpochMilli)
      .extracting(instant -> LocalDateTime.ofInstant(instant, zone))
      .isEqualTo(LocalDateTime.of(YEAR, MONTH, DAY_OF_MONTH, HOUR, MINUTE, SECOND + 1));
  }

  @Test
  void calculateNextMinuteReset() {
    TimeCalculator calculator = new TimeCalculator();

    when(clock.get()).thenReturn(TEST_DATE.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

    Long time = calculator.calculateNextReset(clock, 1L, TimeUnit.MINUTES);

    assertThat(time).isNotNull()
      .extracting(Instant::ofEpochMilli)
      .extracting(instant -> LocalDateTime.ofInstant(instant, zone))
      .isEqualTo(LocalDateTime.of(YEAR, MONTH, DAY_OF_MONTH, HOUR, MINUTE + 1, 0));
  }

  @Test
  void calculateNextHourReset() {
    TimeCalculator calculator = new TimeCalculator();

    when(clock.get()).thenReturn(TEST_DATE.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

    Long time = calculator.calculateNextReset(clock, 1L, TimeUnit.HOURS);

    assertThat(time).isNotNull()
      .extracting(Instant::ofEpochMilli)
      .extracting(instant -> LocalDateTime.ofInstant(instant, zone))
      .isEqualTo(LocalDateTime.of(YEAR, MONTH, DAY_OF_MONTH, HOUR + 1, 0));
  }

  @Test
  void calculateNextDayReset() {
    TimeCalculator calculator = new TimeCalculator();

    when(clock.get()).thenReturn(TEST_DATE.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

    Long time = calculator.calculateNextReset(clock, 1L, TimeUnit.DAYS);

    assertThat(time).isNotNull()
      .extracting(Instant::ofEpochMilli)
      .extracting(instant -> LocalDateTime.ofInstant(instant, zone))
      .isEqualTo(LocalDateTime.of(YEAR, MONTH, DAY_OF_MONTH + 1, 0, 0));
  }
}