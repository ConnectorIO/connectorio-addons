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
package org.connectorio.chrono.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.connectorio.chrono.Period.DAY;
import static org.connectorio.chrono.Period.HALF_HOUR;
import static org.connectorio.chrono.Period.HOUR;
import static org.connectorio.chrono.Period.MINUTE;
import static org.connectorio.chrono.Period.MONTH;
import static org.connectorio.chrono.Period.QUARTER_HOUR;
import static org.connectorio.chrono.Period.SECOND;
import static org.connectorio.chrono.Period.WEEK;
import static org.connectorio.chrono.Period.YEAR;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.connectorio.chrono.Period;
import org.connectorio.chrono.PeriodCalculator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FuturePeriodCalculatorTest {

  @MethodSource
  @ParameterizedTest
  void testPeriods(Argument argument) {
    PeriodCalculator calculator = new FuturePeriodCalculator(Clock.fixed(argument.instant, ZoneId.of("GMT")), argument.period);

    ZonedDateTime result = calculator.calculate();

    assertThat(result)
      .isEqualTo(argument.result);
  }

  private static List<Argument> testPeriods() {
    return Arrays.asList(
      new Argument("2020-12-03T10:36:31.501", "2021-01-01T00:00", YEAR),
      new Argument("2020-12-03T10:36:31.501", "2021-01-01T00:00", MONTH),
      new Argument("2020-12-03T10:36:31.501", "2020-12-07T00:00", WEEK),
      new Argument("2020-12-03T10:36:31.501", "2020-12-04T00:00", DAY),
      new Argument("2020-12-03T10:36:31.501", "2020-12-03T11:00", HOUR),
      new Argument("2020-12-03T10:36:31.501", "2020-12-03T11:00", HALF_HOUR),
      new Argument("2020-12-03T10:46:31.501", "2020-12-03T11:00", QUARTER_HOUR),
      new Argument("2020-12-03T10:36:31.501", "2020-12-03T10:37:00", MINUTE),
      new Argument("2020-12-03T10:36:31.501", "2020-12-03T10:36:32", SECOND)
    );
  }

  static class Argument {

    static DateTimeFormatter FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Instant instant;
    public ZonedDateTime result;
    public Period period;

    Argument(String date, String result, Period period) {
      this.instant = LocalDateTime.parse(date, FORMAT).atZone(ZoneId.of("GMT")).toInstant();
      this.period = period;
      this.result = LocalDateTime.parse(result, FORMAT).atZone(ZoneId.of("GMT"));
    }

    @Override
    public String toString() {
      return "instant=" + instant + ", period=" + period + ", result=" + result;
    }
  }

}