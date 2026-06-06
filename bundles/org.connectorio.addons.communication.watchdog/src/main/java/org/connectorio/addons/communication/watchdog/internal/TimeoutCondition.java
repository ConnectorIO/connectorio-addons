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

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.connectorio.addons.communication.watchdog.WatchdogClock;
import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutCondition implements WatchdogCondition {

  private final Logger logger = LoggerFactory.getLogger(TimeoutCondition.class);
  private final AtomicReference<State> state = new AtomicReference<>();

  private final WatchdogClock clock;
  private final long timeoutPeriodMs;
  private final ChannelUID channel;

  private final AtomicLong lastUpdate = new AtomicLong(Long.MIN_VALUE);

  public TimeoutCondition(WatchdogClock clock, ChannelUID channel, long timeoutPeriodMs) {
    this.clock = clock;
    this.timeoutPeriodMs = timeoutPeriodMs;
    this.channel = channel;
  }

  @Override
  public State getState() {
    return state.get();
  }

  @Override
  public State evaluate() {
    long updateWindow = clock.getTimestamp() - timeoutPeriodMs;
    if (this.lastUpdate.compareAndSet(Long.MIN_VALUE, updateWindow)) {
      return State.INITIALIZED;
    }
    long last = this.lastUpdate.get();
    logger.debug("Last update time {} should be higher than {}", last, updateWindow);
    State currentState = last > updateWindow ? State.OK : State.FAILED;
    state.set(currentState);
    return currentState;
  }

  @Override
  public long getInterval() {
    return timeoutPeriodMs;
  }

  @Override
  public void mark() {
    long now = clock.getTimestamp();
    this.lastUpdate.set(now);
    logger.debug("Channel {} received update at {}.", channel, now);
  }

  @Override
  public ChannelUID getChannel() {
    return channel;
  }

  @Override
  public String toString() {
    return "TimeoutCondition [" + channel + ", timeout=" + timeoutPeriodMs + "ms]";
  }
}
