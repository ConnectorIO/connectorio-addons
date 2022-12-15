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
import java.util.Locale;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Representation day of week.
 */
public enum WeekInMonthType implements State, Command {

  FIRST {
    protected boolean matches(LocalDate date) {
      return date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 7;
    }
  },
  SECOND {
    protected boolean matches(LocalDate date) {
      return date.getDayOfMonth() >= 8 && date.getDayOfMonth() <= 14;
    }
  },
  THIRD {
    protected boolean matches(LocalDate date) {
      return date.getDayOfMonth() >= 15 && date.getDayOfMonth() <= 21;
    }
  },
  FOURTH {
    protected boolean matches(LocalDate date) {
      return date.getDayOfMonth() >= 22 && date.getDayOfMonth() <= 28;
    }
  },

  FIFTH {
    protected boolean matches(LocalDate date) {
      return date.getDayOfMonth() >= 29 && date.getDayOfMonth() <= 31;
    }
  },
  LAST_7_DAYS {
    protected boolean matches(LocalDate date) {
      int maxLength = date.getMonth().maxLength();
      return date.getDayOfMonth() >= (maxLength - 6) && date.getDayOfMonth() <= maxLength;
    }
  };

  public boolean matches(LocalDateType dateType) {
    return matches(dateType.getDate());
  }

  protected boolean matches(LocalDate date) {
    throw new AbstractMethodError();
  }

  @Override
  public String format(String pattern) {
    return String.format(pattern, name());
  }

  public String format(Locale locale, String pattern) {
    return String.format(locale, pattern, name());
  }

  @Override
  public String toFullString() {
    return name();
  }

  @Override
  public <T extends State> T as(Class<T> target) {
    return State.super.as(target);
  }

  @Override
  public String toString() {
    return "WeekInMonth [" + toFullString() + "]";
  }

}
