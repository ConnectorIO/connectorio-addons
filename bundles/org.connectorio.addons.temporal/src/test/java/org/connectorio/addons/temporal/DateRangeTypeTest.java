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
package org.connectorio.addons.temporal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;

class DateRangeTypeTest {

  @Test
  public void testStringConversion() {
    LocalDate from = LocalDate.of(2022, 12, 12);
    LocalDate to = LocalDate.of(2022, 12, 14);
    LocalDateRangeType dateType = new LocalDateRangeType(new LocalDateType(from), new LocalDateType(to));

    String stringRepr = dateType.toFullString();
    assertThat(stringRepr).isNotNull().hasLineCount(1);

    LocalDateRangeType reconstructed = new LocalDateRangeType(stringRepr);
    assertThat(reconstructed).isNotNull().isEqualTo(dateType);
  }

  @Test
  public void testDateTimeCast() {
    LocalDate from = LocalDate.of(2022, 12, 12);
    LocalDate to = LocalDate.of(2022, 12, 14);
    LocalDateRangeType dateType = new LocalDateRangeType(new LocalDateType(from), new LocalDateType(to));

    DateTimeType fromTimeType = dateType.getFrom().as(DateTimeType.class);
    DateTimeType toTimeType = dateType.getTo().as(DateTimeType.class);

    assertThat(fromTimeType).isNotNull()
      .matches(val -> from.equals(val.getZonedDateTime().toLocalDate()));
    assertThat(toTimeType).isNotNull()
      .matches(val -> to.equals(val.getZonedDateTime().toLocalDate()));
  }

}