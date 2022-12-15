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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class CalendarType implements State, Command {

  private final List<CalendarEntryType> events;

  public CalendarType(List<CalendarEntryType> events) {
    this.events = events;
  }

  @Override
  public String format(String pattern) {
    return String.format(pattern, this);
  }

  @Override
  public String toFullString() {
    StringBuilder builder = new StringBuilder().append('[');
    for (int index = 0, size = events.size(); index < size; index++) {
      CalendarEntryType entry = events.get(index);
      builder.append(entry.toFullString());
      if (index + 1 < events.size()) {
        builder.append(',');
      }
    }
    return builder.append(']').toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CalendarType)) {
      return false;
    }
    CalendarType that = (CalendarType) o;
    return Objects.equals(events, that.events);
  }

  @Override
  public int hashCode() {
    return Objects.hash(events);
  }

  public static CalendarType parse(String value) {
    if (value.startsWith("[") && value.startsWith("]")) {
      // cut first and last character
      String[] entries = value.substring(1, value.length() - 2).split(",");
      List<CalendarEntryType> calendarEntries = new ArrayList<>();
      for (String entry : entries) {
        calendarEntries.add(CalendarEntryType.parse(entry));
      }
    }

    throw new IllegalArgumentException("Could not parse calendar value: " + value);
  }

}
