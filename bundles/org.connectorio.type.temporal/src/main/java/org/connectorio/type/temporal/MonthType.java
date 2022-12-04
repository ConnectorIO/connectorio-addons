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

import java.time.Month;
import java.util.Locale;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.State;

/**
 * Representation day of week.
 */
public enum MonthType implements State, Command, PrimitiveType {

  JANUARY(1),
  FEBRUARY(2),
  MARCH(3),
  APRIL(4),
  MAY(5),
  JUNE(6),
  JULY(7),
  AUGUST(8),
  SEPTEMBER(9),
  OCTOBER(10),
  NOVEMBER(11),
  DECEMBER(12);

  private final Month month;

  private MonthType(int month) {
    this.month = Month.of(month);
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
    return month.name();
  }

  @Override
  public <T extends State> T as(Class<T> target) {
    return State.super.as(target);
  }

  @Override
  public String toString() {
    return "Month [" + toFullString() + "]";
  }

}
