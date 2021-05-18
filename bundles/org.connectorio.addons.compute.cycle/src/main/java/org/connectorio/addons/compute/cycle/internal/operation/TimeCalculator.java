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
package org.connectorio.addons.compute.cycle.internal.operation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimeCalculator {

  public Long calculateNextReset(Supplier<Long> clock, Long duration, TimeUnit unit) {
    Instant temporal = Instant.ofEpochMilli(clock.get());
    ZoneId zone = ZoneId.systemDefault();
    LocalDateTime time = LocalDateTime.ofInstant(temporal, zone);

    switch (unit) {
      case DAYS:
        LocalTime midnight = LocalTime.of(0, 0);
        time = time.plusDays(duration).with(midnight);
        break;
      case HOURS:
        time = time.plusHours(duration).withMinute(0).withSecond(0).withNano(0);
        break;
      case MINUTES:
        time = time.plusMinutes(duration.intValue()).withSecond(0).withNano(0);
        break;
      case SECONDS:
        time = time.plusSeconds(duration.intValue()).withNano(0);
        break;
    }

    return time.atZone(zone).toInstant().toEpochMilli();
  }

}
