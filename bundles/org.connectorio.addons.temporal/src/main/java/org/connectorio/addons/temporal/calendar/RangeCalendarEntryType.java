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
package org.connectorio.addons.temporal.calendar;

import java.time.LocalDate;
import org.connectorio.addons.temporal.LocalDateRangeType;
import org.connectorio.addons.temporal.LocalDateType;

public class RangeCalendarEntryType extends CalendarEntryType {

  private LocalDateRangeType dateRange;

  public RangeCalendarEntryType(LocalDateRangeType dateRangeType) {
    this.dateRange = dateRangeType;
  }

  @Override
  public boolean matches(LocalDateType dateType) {
    LocalDate date = dateType.getDate();
    LocalDate from = dateRange.getFrom().getDate();
    LocalDate to = dateRange.getTo().getDate();
    return (from.isEqual(date) ||  from.isBefore(date)) &&
      (to.isEqual(date) || to.isAfter(date));
  }

  @Override
  public String format(String pattern) {
    return String.format(pattern, this);
  }

  @Override
  public String toFullString() {
    return "{}";
  }

  public String toString() {
    return toFullString();
  }

}