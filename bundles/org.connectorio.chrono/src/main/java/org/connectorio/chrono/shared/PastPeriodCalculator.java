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
package org.connectorio.chrono.shared;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.connectorio.chrono.Period;
import org.connectorio.chrono.PeriodCalculator;

public class PastPeriodCalculator implements PeriodCalculator {

  private final Clock clock;
  private final int offset;
  private final Period period;

  public PastPeriodCalculator(Clock clock, Period period) {
    this(clock, 0, period);
  }

  public PastPeriodCalculator(Clock clock, int offset, Period period) {
    this.clock = clock;
    this.offset = offset;
    this.period = period;
  }

  public ZonedDateTime calculate() {
    ZonedDateTime time = ZonedDateTime.ofInstant(clock.instant(), clock.getZone());
    LocalTime midnight = LocalTime.of(0, 0, 0, 0);

    switch (period) {
      case YEAR:
        time = time.minusYears(offset).withDayOfYear(1).with(midnight);
        break;
      case MONTH:
        time = time.minusMonths(offset).withDayOfMonth(1).with(midnight);
        break;
      case WEEK:
        time = time.minusWeeks(offset).with(DayOfWeek.MONDAY).with(midnight);
        break;
      case DAY:
        time = time.minusDays(offset).with(midnight);
        break;
      case HOUR:
        time = time.minusHours(offset).withMinute(0).withSecond(0).withNano(0);
        break;
      case HALF_HOUR:
        time = time.withMinute(0).withSecond(0).withNano(0).plusMinutes((time.getMinute() / 30) * 30).minusMinutes(offset * 30L);
        break;
      case QUARTER_HOUR:
        time = time.withMinute(0).withSecond(0).withNano(0).plusMinutes((time.getMinute() / 15) * 15).minusMinutes(offset * 15L);
        break;
      case MINUTE:
        time = time.minusMinutes(offset).withSecond(0).withNano(0);
        break;
      case SECOND:
        time = time.minusSeconds(offset).withNano(0);
        break;
    }

    return time;
  }

  public Period getPeriod() {
    return period;
  }

  public String toString() {
    return "Past Period Calculator [" + period + "]";
  }

}
