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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.ComplexType;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.State;

/**
 * Representation of day schedule which is composed of multiple time entries and values assigned to these.
 *
 * Day schedule do not operate with time ranges, it rather assumes that next entry overrides earlier one.
 */
public class DayScheduleType implements Command, State, ComplexType {
  private final static Pattern REGEX_PATTERN = Pattern.compile("\\s*\"(?<time>[0-9:\\-+]+)\"\\s*:\\s*\"(?<value>.*?)\"\\s*,?");

  private final SortedMap<LocalTimeType, State> daySchedule;

  public DayScheduleType(Map.Entry<LocalTimeType, State> ... schedule) {
    this(sort(schedule));
  }

  public DayScheduleType(Map<LocalTimeType, State> daySchedule) {
    this.daySchedule = new TreeMap<>();
    this.daySchedule.putAll(daySchedule);
  }

  public DayScheduleType(String schedule) {
    this(parse(schedule));
  }

  private static Map<LocalTimeType, State> parse(String schedule) {
    SortedMap<LocalTimeType, State> map = new TreeMap<>();
    Matcher matcher = REGEX_PATTERN.matcher(schedule);
    while (matcher.find()) {
      String time = matcher.group("time");
      String value = matcher.group("value");

      if (time != null && value != null) {
        map.put(new LocalTimeType(time), new StringType(value));
      }
    }

    return map;
  }

  private static SortedMap<LocalTimeType, State> sort(Entry<LocalTimeType, State>[] schedules) {
    return Arrays.stream(schedules)
      .collect(Collectors.toMap(
        Entry::getKey, Entry::getValue,
        (l, r) -> l,
        TreeMap::new
      ));
  }

  @Override
  public String format(String pattern) {
    return toFullString();
  }

  @Override
  public String toFullString() {
    StringBuilder str = new StringBuilder();
    str.append("[");
    Iterator<Entry<LocalTimeType, State>> iterator = daySchedule.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<LocalTimeType, State> entry = iterator.next();
      str.append("{\"").append(entry.getKey().toFullString()).append("\":\"")
        .append(entry.getValue().toFullString()).append("\"}");
      if (iterator.hasNext()) {
        str.append(",");
      }
    }
    return str.append("]").toString();
  }

  @Override
  public SortedMap<String, PrimitiveType> getConstituents() {
    return null;
  }

  @Override
  public String toString() {
    return "DaySchedule " + toFullString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DayScheduleType)) {
      return false;
    }
    DayScheduleType that = (DayScheduleType) o;
    return Objects.equals(daySchedule, that.daySchedule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(daySchedule);
  }

}
