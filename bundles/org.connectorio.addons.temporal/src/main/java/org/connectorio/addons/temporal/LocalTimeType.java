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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Representation of local time.
 */
public class LocalTimeType implements State, Command, Comparable<LocalTimeType> {

  public static final DateTimeFormatter FORMAT_LOCAL = DateTimeFormatter.ISO_LOCAL_TIME;

  private final LocalTime time;

  public LocalTimeType(LocalTime time) {
    this.time = time;
  }

  public LocalTimeType(String time) {
    this(parse(time));
  }

  public LocalTime getTime() {
    return time;
  }

  public static LocalTime parse(String time) {
    try {
      return LocalTime.parse(time, FORMAT_LOCAL);
    } catch (DateTimeParseException e2) {
      throw new IllegalArgumentException("Could not parse value", e2);
    }
  }

  @Override
  public String format(String pattern) {
    if (pattern == null) {
      return FORMAT_LOCAL.format(time);
    }

    return String.format(pattern, time);
  }

  public String format(Locale locale, String pattern) {
    return String.format(locale, pattern, time);
  }

  @Override
  public String toFullString() {
    return FORMAT_LOCAL.format(time);
  }

  @Override
  public <T extends State> T as(Class<T> target) {
    if (target == DateTimeType.class) {
      return (T) new DateTimeType(toFullString());
    }
    return State.super.as(target);
  }

  @Override
  public String toString() {
    return "T" + toFullString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocalTimeType)) {
      return false;
    }
    LocalTimeType timeType = (LocalTimeType) o;
    return Objects.equals(time, timeType.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time);
  }

  @Override
  public int compareTo(LocalTimeType o) {
    return this.time.compareTo(o.time);
  }
  }
