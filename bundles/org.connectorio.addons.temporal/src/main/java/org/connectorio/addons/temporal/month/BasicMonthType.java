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
package org.connectorio.addons.temporal.month;

import java.time.Month;
import java.util.Locale;
import org.connectorio.addons.temporal.LocalDateType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Representation standard month.
 */
public class BasicMonthType extends MonthType implements State, Command {

  public static MonthType JANUARY = new BasicMonthType(1);
  public static MonthType FEBRUARY = new BasicMonthType(2);
  public static MonthType MARCH = new BasicMonthType(3);
  public static MonthType APRIL = new BasicMonthType(4);
  public static MonthType MAY = new BasicMonthType(5);
  public static MonthType JUNE = new BasicMonthType(6);
  public static MonthType JULY = new BasicMonthType(7);
  public static MonthType AUGUST = new BasicMonthType(8);
  public static MonthType SEPTEMBER = new BasicMonthType(9);
  public static MonthType OCTOBER = new BasicMonthType(10);
  public static MonthType NOVEMBER = new BasicMonthType(11);
  public static MonthType DECEMBER = new BasicMonthType(12);

  private final Month month;

  private BasicMonthType(int month) {
    this(Month.of(month));
  }

  private BasicMonthType(Month month) {
    this.month = month;
  }

  @Override
  public boolean matches(LocalDateType date) {
    return month == date.getDate().getMonth();
  }

  public static MonthType parse(String value) {
    return new BasicMonthType(Month.valueOf(value.trim().toUpperCase()));
  }

  @Override
  public String format(String pattern) {
    return String.format(pattern, month);
  }

  public String format(Locale locale, String pattern) {
    return String.format(locale, pattern, month);
  }

  @Override
  public String toFullString() {
    return '"' + month.name() + '"';
  }

  @Override
  public String toString() {
    return toFullString();
  }
}
