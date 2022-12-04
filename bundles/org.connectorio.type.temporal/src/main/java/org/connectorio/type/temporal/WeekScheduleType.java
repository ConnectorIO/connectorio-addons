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

import java.util.Arrays;
import java.util.Objects;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openhab.core.types.Command;
import org.openhab.core.types.ComplexType;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.State;

/**
 * Representation of week schedule with first day assumed to be Monday and last one being Sunday.
 */
public class WeekScheduleType implements Command, State, ComplexType {

  private final static Pattern REGEX_PATTERN = Pattern.compile("\\s*\"(?<day>\\d+)\"\\s*:\\s*(?<schedule>\\[.*?\\])\\s*");

  private final DayScheduleType[] weekSchedule;

  public WeekScheduleType() {
    this(new DayScheduleType());
  }

  public WeekScheduleType(DayScheduleType mon, DayScheduleType tue, DayScheduleType wed,
    DayScheduleType thu, DayScheduleType fri, DayScheduleType sat, DayScheduleType sun) {
    weekSchedule = new DayScheduleType[7];
    weekSchedule[0] = mon;
    weekSchedule[1] = tue;
    weekSchedule[2] = wed;
    weekSchedule[3] = thu;
    weekSchedule[4] = fri;
    weekSchedule[5] = sat;
    weekSchedule[6] = sun;
  }

  public WeekScheduleType(DayScheduleType schedule) {
    this(schedule, schedule, schedule, schedule, schedule, schedule, schedule);
  }

  public WeekScheduleType(String schedule) {
    weekSchedule = parse(schedule);
  }

  private DayScheduleType[] parse(String schedule) {
    DayScheduleType[] weekSchedule = new DayScheduleType[7];
    Matcher matcher = REGEX_PATTERN.matcher(schedule);
    while (matcher.find()) {
      int day = Integer.parseInt(matcher.group("day"));
      if (day < 1 || day > 7) {
        throw new IllegalArgumentException("Invalid day index '"+ day + "' for week schedule");
      }
      if (weekSchedule[day - 1] != null) {
        throw new IllegalArgumentException("Duplicate day with index '"+ day + "'");
      }
      String daySchedule = matcher.group("schedule");
      weekSchedule[day - 1] = new DayScheduleType(daySchedule);
    }
    return weekSchedule;
  }

  public WeekScheduleType withMondaySchedule(DayScheduleType schedule) {
    return new WeekScheduleType(
      schedule, weekSchedule[1], weekSchedule[2], weekSchedule[3], weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public WeekScheduleType withTuesdaySchedule(DayScheduleType schedule) {
    return new WeekScheduleType(
        weekSchedule[0], schedule, weekSchedule[2], weekSchedule[3], weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public WeekScheduleType withWednesdaySchedule(DayScheduleType schedule) {
    return new WeekScheduleType(
      weekSchedule[0], weekSchedule[1], schedule, weekSchedule[3], weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public WeekScheduleType withThursdaySchedule(DayScheduleType schedule) {
    return new WeekScheduleType(
      weekSchedule[0], weekSchedule[1], weekSchedule[2], schedule, weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public WeekScheduleType withFridaySchedule(DayScheduleType schedule) {
    return new WeekScheduleType(
      weekSchedule[0], weekSchedule[1], weekSchedule[2], weekSchedule[3], schedule, weekSchedule[5], weekSchedule[6]
    );
  }

  public WeekScheduleType withSaturdaySchedule(DayScheduleType schedule) {
    return new WeekScheduleType(
      weekSchedule[0], weekSchedule[1], weekSchedule[2], weekSchedule[3], weekSchedule[4], schedule, weekSchedule[6]
    );
  }

  public WeekScheduleType withSundaySchedule(DayScheduleType schedule) {
    return new WeekScheduleType(
      weekSchedule[0], weekSchedule[1], weekSchedule[2], weekSchedule[3], weekSchedule[4], weekSchedule[5], schedule
    );
  }

  @Override
  public String format(String pattern) {
    return toFullString();
  }

  @Override
  public String toFullString() {
    StringBuilder str = new StringBuilder();
    str.append('{');
    for (int index = 0; index < weekSchedule.length; index++) {
      DayScheduleType day = weekSchedule[index];
      str.append('"').append(index + 1).append('"').append(':').append(day.toFullString());
      if (index + 1 < weekSchedule.length) {
        str.append(',');
      }
    }
    str.append('}');
    return str.toString();
  }

  @Override
  public SortedMap<String, PrimitiveType> getConstituents() {
    return null;
  }

  @Override
  public String toString() {
    return "Week Schedule " + toFullString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WeekScheduleType)) {
      return false;
    }
    WeekScheduleType that = (WeekScheduleType) o;
    return Arrays.equals(weekSchedule, that.weekSchedule);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(weekSchedule);
  }

}
