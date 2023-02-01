/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.communication.watchdog.internal;

import java.time.Clock;
import org.connectorio.addons.communication.watchdog.WatchdogClock;

public class DefaultWatchdogClock implements WatchdogClock {

  private final Clock clock;

  public DefaultWatchdogClock() {
    this(Clock.systemUTC());
  }

  public DefaultWatchdogClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public long getTimestamp() {
    return clock.millis();
  }

}
