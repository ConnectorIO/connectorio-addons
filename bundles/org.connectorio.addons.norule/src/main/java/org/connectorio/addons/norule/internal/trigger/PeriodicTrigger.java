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
package org.connectorio.addons.norule.internal.trigger;

import org.connectorio.addons.norule.Periodic;
import org.connectorio.addons.norule.Scheduled;
import org.connectorio.addons.norule.Trigger;
import org.connectorio.chrono.Period;

public class PeriodicTrigger implements Trigger, Periodic {

  private final long delay;
  private final Period period;

  public PeriodicTrigger(long delay, Period period) {
    this.delay = delay;
    this.period = period;
  }

  @Override
  public long getDelay() {
    return delay;
  }

  @Override
  public Period getPeriod() {
    return period;
  }

  public String toString() {
    return "every " + delay + " " + period + " (periodic)";
  }

}
