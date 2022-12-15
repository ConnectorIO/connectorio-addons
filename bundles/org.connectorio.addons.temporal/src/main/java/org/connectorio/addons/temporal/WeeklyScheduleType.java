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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Representation of week schedule with first day assumed to be Monday and last one being Sunday.
 */
public class WeeklyScheduleType implements Command, State {

  private final static Pattern REGEX_PATTERN = Pattern.compile("\\s*\"(?<day>\\w+)\"\\s*:\\s*(?<schedule>\\[.*?\\])\\s*");

  private final DayScheduleType[] weekSchedule;

  public WeeklyScheduleType() {
    this(new DayScheduleType());
  }

  public WeeklyScheduleType(DayScheduleType mon, DayScheduleType tue, DayScheduleType wed,
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

  public WeeklyScheduleType(DayScheduleType schedule) {
    this(schedule, schedule, schedule, schedule, schedule, schedule, schedule);
  }

  public WeeklyScheduleType(String schedule) {
    weekSchedule = parse(schedule);
  }

  public DayScheduleType getMondaySchedule() {
    return weekSchedule[0];
  }

  public WeeklyScheduleType withMondaySchedule(DayScheduleType schedule) {
    return new WeeklyScheduleType(
      schedule, weekSchedule[1], weekSchedule[2], weekSchedule[3], weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public DayScheduleType getTuesdaySchedule() {
    return weekSchedule[1];
  }

  public WeeklyScheduleType withTuesdaySchedule(DayScheduleType schedule) {
    return new WeeklyScheduleType(
        weekSchedule[0], schedule, weekSchedule[2], weekSchedule[3], weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public DayScheduleType getWednesdaySchedule() {
    return weekSchedule[2];
  }

  public WeeklyScheduleType withWednesdaySchedule(DayScheduleType schedule) {
    return new WeeklyScheduleType(
      weekSchedule[0], weekSchedule[1], schedule, weekSchedule[3], weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public DayScheduleType getThursdaySchedule() {
    return weekSchedule[3];
  }

  public WeeklyScheduleType withThursdaySchedule(DayScheduleType schedule) {
    return new WeeklyScheduleType(
      weekSchedule[0], weekSchedule[1], weekSchedule[2], schedule, weekSchedule[4], weekSchedule[5], weekSchedule[6]
    );
  }

  public DayScheduleType getFridaySchedule() {
    return weekSchedule[4];
  }

  public WeeklyScheduleType withFridaySchedule(DayScheduleType schedule) {
    return new WeeklyScheduleType(
      weekSchedule[0], weekSchedule[1], weekSchedule[2], weekSchedule[3], schedule, weekSchedule[5], weekSchedule[6]
    );
  }

  public DayScheduleType getSaturdaySchedule() {
    return weekSchedule[5];
  }

  public WeeklyScheduleType withSaturdaySchedule(DayScheduleType schedule) {
    return new WeeklyScheduleType(
      weekSchedule[0], weekSchedule[1], weekSchedule[2], weekSchedule[3], weekSchedule[4], schedule, weekSchedule[6]
    );
  }

  public DayScheduleType getSundaySchedule() {
    return weekSchedule[6];
  }

  public WeeklyScheduleType withSundaySchedule(DayScheduleType schedule) {
    return new WeeklyScheduleType(
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
    boolean haveAtLeastOneDay = false;
    for (int index = 0; index < weekSchedule.length; index++) {
      DayScheduleType day = weekSchedule[index];
      if (day != null) {
        str.append('"').append(DayOfWeekType.values()[index].name()).append('"').append(':').append(day.toFullString());
        if (!haveAtLeastOneDay) {
          haveAtLeastOneDay = true;
        } else {
          if (index + 1 < weekSchedule.length) {
            str.append(',');
          }
        }
      }
    }
    str.append('}');
    return str.toString();
  }

  @Override
  public String toString() {
    return toFullString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WeeklyScheduleType)) {
      return false;
    }
    WeeklyScheduleType that = (WeeklyScheduleType) o;
    return Arrays.equals(weekSchedule, that.weekSchedule);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(weekSchedule);
  }

  public static WeeklyScheduleType valueOf(String value) {
    return new WeeklyScheduleType(value);
  }

  private DayScheduleType[] parse(String schedule) {
    DayScheduleType[] weekSchedule = new DayScheduleType[7];
    Matcher matcher = REGEX_PATTERN.matcher(schedule);
    while (matcher.find()) {
      int day = -1;
      try {
        DayOfWeekType dayOfWeek = DayOfWeekType.valueOf(matcher.group("day"));
        day = dayOfWeek.getNumber();
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid day '" + matcher.group("day") + "'");
      }
      if (weekSchedule[day - 1] != null) {
        throw new IllegalArgumentException("Duplicate day with index '"+ matcher.group("day") + "'");
      }
      String daySchedule = matcher.group("schedule");
      weekSchedule[day - 1] = new DayScheduleType(daySchedule);
    }
    return weekSchedule;
  }

}
