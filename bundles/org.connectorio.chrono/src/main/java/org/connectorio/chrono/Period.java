/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.chrono;

/**
 * Definition of time periods.
 *
 * This is static set which defines possible calculations. Each period has an beginning and end, however role of this
 * type is just specification of known time spans. Actual logic needed to find period edges should be supplied by
 * PeriodCalculator.
 */
public enum Period {
  YEAR ("Every year", "Y") {
    @Override
    public Period smaller() {
      return MONTH;
    }

    @Override
    public Period larger() {
      return null;
    }
  },
  MONTH ("Every month", "M") {
    @Override
    public Period smaller() {
      return WEEK;
    }

    @Override
    public Period larger() {
      return YEAR;
    }
  },
  WEEK ("Every week", "W") {
    @Override
    public Period smaller() {
      return DAY;
    }

    @Override
    public Period larger() {
      return MONTH;
    }
  },
  DAY ("Every day", "D") {
    @Override
    public Period smaller() {
      return HOUR;
    }

    @Override
    public Period larger() {
      return WEEK;
    }
  },
  HOUR ("Every hour", "h") {
    @Override
    public Period smaller() {
      return MINUTE;
    }

    @Override
    public Period larger() {
      return DAY;
    }
  },
  HALF_HOUR ("Every half hour", "30m", false) {
    @Override
    public Period smaller() {
      return MINUTE;
    }

    @Override
    public Period larger() {
      return HOUR;
    }
  },
  QUARTER_HOUR("Every quarter of an hour", "15m", false) {
    @Override
    public Period smaller() {
      return MINUTE;
    }

    @Override
    public Period larger() {
      return HOUR;
    }
  },
  MINUTE ("Every minute", "m") {
    @Override
    public Period smaller() {
      return SECOND;
    }

    @Override
    public Period larger() {
      return HOUR;
    }
  },
  SECOND ("Every second", "s") {
    @Override
    public Period smaller() {
      return null;
    }

    @Override
    public Period larger() {
      return MINUTE;
    }
  };

  private final String label;
  private final String symbol;
  private final boolean rounded;

  Period(String label, String symbol) {
    this(label, symbol, true);
  }

  Period(String label, String symbol, boolean rounded) {
    this.label = label;
    this.symbol = symbol;
    this.rounded = rounded;
  }

  public boolean isRounded() {
    return rounded;
  }

  public Period smaller() {
    throw new AbstractMethodError();
  }

  public Period larger() {
    throw new AbstractMethodError();
  }

  public String getSymbol() {
    return symbol;
  }

  public String getLabel() {
    return label;
  }

}
