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
import org.connectorio.addons.temporal.LocalDateType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Extended standard month.
 */
public abstract class ExtendedMonthType extends MonthType implements State, Command {

  public static final String ODD_MONTHS_VALUE = "ODD_MONTHS";
  public static final String EVEN_MONTHS_VALUE = "EVEN_MONTHS";
  public static final String ANY_MONTH_VALUE = "ANY_MONTH";

  public static MonthType ODD_MONTHS = new ExtendedMonthType() {
    @Override
    public boolean matches(LocalDateType date) {
      Month month = date.getDate().getMonth();
      switch (month) {
        case JANUARY:
        case MARCH:
        case MAY:
        case JULY:
        case SEPTEMBER:
        case NOVEMBER:
          return true;
      }
      return false;
    }

    @Override
    public String toFullString() {
      return "\"" + ODD_MONTHS_VALUE + "\"";
    }
  };

  public static MonthType EVEN_MONTHS = new ExtendedMonthType() {
    @Override
    public boolean matches(LocalDateType date) {
      Month month = date.getDate().getMonth();
      switch (month) {
        case FEBRUARY:
        case APRIL:
        case JUNE:
        case AUGUST:
        case OCTOBER:
        case DECEMBER:
          return true;
      }
      return false;
    }

    @Override
    public String toFullString() {
      return "\"" + EVEN_MONTHS_VALUE + "\"";
    }
  };

  public static MonthType ANY = new ExtendedMonthType() {
    @Override
    public boolean matches(LocalDateType date) {
      return true;
    }

    @Override
    public String format(String pattern) {
      return null;
    }

    @Override
    public String toFullString() {
      return "\"" + ANY_MONTH_VALUE + "\"";
    }
  };

  public static MonthType parse(String value) {
    if (ANY_MONTH_VALUE.equals(value)) {
      return ExtendedMonthType.ANY;
    }
    if (ODD_MONTHS_VALUE.equals(value)) {
      return ExtendedMonthType.ODD_MONTHS;
    }
    if (EVEN_MONTHS_VALUE.equals(value)) {
      return ExtendedMonthType.EVEN_MONTHS;
    }

    throw new IllegalArgumentException("Unknown value " + value);
  }

  @Override
  public String format(String pattern) {
    return String.format(pattern, this);
  }

  @Override
  public String toString() {
    return toFullString();
  }

}
