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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Representation of date which is local to an installation or controller which is not capable of
 * distinguishing a time zone.
 */
public class LocalDateType implements State, Command {

  private final static DateTimeFormatter ISO_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

  private final LocalDate date;
  public LocalDateType(LocalDate date) {
    this.date = date;
  }

  public LocalDateType(String date) {
    this(LocalDate.parse(date, ISO_LOCAL_DATE));
  }

  public LocalDateType(ZonedDateTime date) {
    this(date.toLocalDate());
  }

  public LocalDate getDate() {
    return date;
  }

  @Override
  public String format(String pattern) {
    if (pattern == null) {
      return ISO_LOCAL_DATE.format(date);
    }

    return String.format(pattern, date);
  }

  public String format(Locale locale, String pattern) {
    return String.format(locale, pattern, date);
  }

  @Override
  public String toFullString() {
    return ISO_LOCAL_DATE.format(date);
  }

  @Override
  public <T extends State> T as(Class<T> target) {
    if (target == DateTimeType.class) {
      return (T) new DateTimeType(ZonedDateTime.of(date, LocalTime.of(0, 0), ZoneId.systemDefault()));
    }
    return State.super.as(target);
  }

  @Override
  public String toString() {
    return "DateType [" + toFullString() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocalDateType)) {
      return false;
    }
    LocalDateType dateType = (LocalDateType) o;
    return Objects.equals(date, dateType.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date);
  }

}
