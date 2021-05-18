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
  YEAR ("Every year"),
  MONTH ("Every month"),
  WEEK ("Every week"),
  DAY ("Every day"),
  HOUR ("Every hour"),
  HALF_HOUR ("Every half hour"),
  QUARTER_HOUR("Every quarter of an hour"),
  MINUTE ("Every minute"),
  SECOND ("Every second");

  private final String label;

  Period(String description) {
    this.label = description;
  }

  public String getLabel() {
    return label;
  }

}
