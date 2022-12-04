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

import java.util.Locale;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openhab.core.types.Command;
import org.openhab.core.types.ComplexType;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.State;

/**
 * Range constructed from two dates indicating boundaries of range itself.
 *
 * This type does not try to validate range itself, it is a holder for 'from' and 'to' values.
 */
public class LocalDateRangeType implements State, Command, ComplexType {

  private static final Pattern REGEX_PATTERN = Pattern.compile("\\{\\s*\"from\"\\s*:\"(?<from>[0-9-]+)\"\\s*,\\s*\"to\"\\s*:\\s*\"(?<to>[0-9-]+)\"\\s*\\}");

  private static final String PATTERN = "";
  private final LocalDateType from;
  private final LocalDateType to;

  public LocalDateRangeType(LocalDateType from, LocalDateType to) {
    this.from = from;
    this.to = to;
  }

  public LocalDateRangeType(LocalDateRangeType range) {
    this(range.from, range.to);
  }

  public LocalDateRangeType(String range) {
    this(parse(range));
  }

  private static LocalDateRangeType parse(String range) {
    Matcher parts = REGEX_PATTERN.matcher(range);
    if (parts.matches() && parts.group("from") != null && parts.group("to") != null) {
      return new LocalDateRangeType(new LocalDateType(parts.group("from")), new LocalDateType(parts.group("to")));
    }
    throw new IllegalArgumentException("Date range '" + range + "' has invalid format");
  }

  public LocalDateType getFrom() {
    return from;
  }

  public LocalDateType getTo() {
    return to;
  }

  @Override
  public SortedMap<String, PrimitiveType> getConstituents() {
    TreeMap<String, PrimitiveType> components = new TreeMap<>();
    components.put("from", getFrom());
    components.put("to", getTo());
    return components;
  }

  @Override
  public String format(String pattern) {
    if (pattern == null) {
      return String.format(PATTERN, from.getDate(), to.getDate());
    }

    return String.format(pattern, from.getDate(), to.getDate());
  }

  public String format(Locale locale, String pattern) {
    return String.format(locale, pattern, from.getDate(), to.getDate());
  }

  @Override
  public String toFullString() {
    return "{\"from\":\"" + from.toFullString() + "\", \"to\":\"" + to.toFullString() + "\"}";
  }

  @Override
  public <T extends State> T as(Class<T> target) {
    return State.super.as(target);
  }

  @Override
  public String toString() {
    return "DateRange [" + toFullString() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocalDateRangeType)) {
      return false;
    }
    LocalDateRangeType that = (LocalDateRangeType) o;
    return Objects.equals(getFrom(), that.getFrom()) &&
      Objects.equals(getTo(), that.getTo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFrom(), getTo());
  }
}
