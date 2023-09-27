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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.connectorio.addons.communication.watchdog.Watchdog;
import org.connectorio.addons.communication.watchdog.WatchdogBuilder;
import org.connectorio.addons.communication.watchdog.WatchdogClock;
import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.connectorio.addons.communication.watchdog.WatchdogListener;
import org.connectorio.addons.link.LinkManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

public class DefaultWatchdogBuilder implements WatchdogBuilder {

  private WatchdogClock clock;
  private DefaultWatchdogManager manager;
  private final LinkManager linkManager;
  private final Thing thing;
  private final Map<ChannelUID, WatchdogCondition> conditions = new HashMap<>();
  private long delayMs;
  private int multiplier;

  public DefaultWatchdogBuilder(WatchdogClock clock, DefaultWatchdogManager manager, LinkManager linkManager, Thing thing) {
    this.clock = clock;
    this.manager = manager;
    this.linkManager = linkManager;
    this.thing = thing;
  }

  @Override
  public WatchdogBuilder withChannel(ChannelUID channel, long timeoutPeriodMs) {
    put(channel, new TimeoutCondition(clock, channel, timeoutPeriodMs));
    return this;
  }

  @Override
  public WatchdogBuilder withChannel(ChannelUID channel, Duration duration) {
    put(channel, new TimeoutCondition(clock, channel, duration.toMillis()));
    return this;
  }

  @Override
  public WatchdogBuilder withChannel(ChannelUID channel, WatchdogCondition condition) {
    put(channel, condition);
    return this;
  }

  @Override
  public WatchdogBuilder withTimeoutDelay(long delayMs) {
    this.delayMs = delayMs;
    return this;
  }

  @Override
  public WatchdogBuilder withTimeoutMultiplier(int multiplier) {
    this.multiplier = multiplier;
    return this;
  }

  @Override
  public Watchdog build(ThingHandlerCallback callback, WatchdogListener listener) {
    DefaultWatchdog watchdog = new DefaultWatchdog(thing, callback, conditions, manager::close);
    manager.registerWatchdog(thing, watchdog, listener);
    return watchdog;
  }

  private void put(ChannelUID channel, WatchdogCondition condition) {
    if (!conditions.containsKey(channel)) {
      conditions.put(channel, condition);
      return;
    }
    WatchdogCondition earlierCondition = conditions.remove(channel);
    conditions.put(channel, new ChainedCondition(earlierCondition, condition));
  }

}
