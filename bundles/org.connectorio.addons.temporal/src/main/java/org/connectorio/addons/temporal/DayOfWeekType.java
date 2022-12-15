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

import java.time.DayOfWeek;
import java.util.Locale;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Representation day of week.
 */
public enum DayOfWeekType implements State, Command {

  MONDAY(1),
  TUESDAY(2),
  WEDNESDAY(3),
  THURSDAY(4),
  FRIDAY(5),
  SATURDAY(6),
  SUNDAY(7);

  private final DayOfWeek day;
  private final int number;

  private DayOfWeekType(int day) {
    this.day = DayOfWeek.of(day);
    this.number = day;
  }

  public DayOfWeek getDay() {
    return day;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public String format(String pattern) {
    return String.format(pattern, day);
  }

  public String format(Locale locale, String pattern) {
    return String.format(locale, pattern, day);
  }

  @Override
  public String toFullString() {
    return day.name();
  }

  @Override
  public <T extends State> T as(Class<T> target) {
    return State.super.as(target);
  }

  @Override
  public String toString() {
    return "DayOfWeek [" + toFullString() + "]";
  }

  public boolean matches(LocalDateType dateType) {
    return false;
  }

}
