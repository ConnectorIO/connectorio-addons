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

import java.util.SortedMap;
import org.openhab.core.types.Command;
import org.openhab.core.types.ComplexType;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.State;

public abstract class CalendarEntryType implements State, Command, ComplexType {

  @Override
  public SortedMap<String, PrimitiveType> getConstituents() {
    return null;
  }

  @Override
  public String format(String pattern) {
    return null;
  }

  @Override
  public String toFullString() {
    return null;
  }

  public boolean matches(LocalDateType dateType) {
    return false;
  }

  public static CalendarEntryType atDate(LocalDateType date) {
    return null;
  }

  public static CalendarEntryType atRange(LocalDateRangeType range) {
    return null;
  }

  public static CalendarEntryType atWeek(MonthType month, WeekInMonthType weekInMonth, DayOfWeekType dayOfWeekType) {
    return null;
  }

}

class RangeCalendarEntryType extends CalendarEntryType {}
class DateCalendarEntryType extends CalendarEntryType {}

class RepeatedCalendarEntryType extends CalendarEntryType {}
