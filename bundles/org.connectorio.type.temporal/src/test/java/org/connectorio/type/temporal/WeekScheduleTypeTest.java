/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.type.temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.StringType;

class WeekScheduleTypeTest {
  @Test
  public void testStringConversion() {
    WeekScheduleType schedule = new WeekScheduleType(
      createDaySchedule()
    );

    String string = schedule.toFullString();
    assertThat(string).isNotNull().hasLineCount(1);

    WeekScheduleType reconstructed = new WeekScheduleType(string);
    assertThat(reconstructed).isNotNull().isEqualTo(schedule);
  }

  @Test
  public void verifyInvalidDay() {
    assertThatThrownBy(() -> new WeekScheduleType("{\"0\":[]}"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("'0'");
  }

  @Test
  public void verifyDuplicateDay() {
    assertThatThrownBy(() -> new WeekScheduleType("{\"5\":[], \"5\":[]}"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("'5'");
  }

  private static DayScheduleType createDaySchedule() {
    LocalTime _06_01 = LocalTime.of(6, 1);
    LocalTime _20_30 = LocalTime.of(20, 30);
    DayScheduleType dateType = new DayScheduleType(
      entry(new LocalTimeType(_06_01), new StringType("1.0")),
      entry(new LocalTimeType(_20_30), new StringType("0.0"))
    );
    return dateType;
  }

}