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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.connectorio.addons.communication.watchdog.Watchdog;
import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWatchdog implements Watchdog {

  private final Logger logger = LoggerFactory.getLogger(DefaultWatchdog.class);
  private final ThingHandlerCallback callback;
  private final Map<WatchdogCondition, Long> conditionIntervalMap;
  private Map<ChannelUID, WatchdogCondition> conditions;
  private Consumer<Watchdog> closeHandler;
  //private final long delayMs;

  public DefaultWatchdog(ThingHandlerCallback callback, Map<ChannelUID, WatchdogCondition> conditions, Consumer<Watchdog> closeHandler) {
    this.callback = callback;
    this.conditionIntervalMap = conditions.entrySet().stream().collect(Collectors.toMap(
      Entry::getValue,
      e -> e.getValue().getInterval(),
      (l, r) -> {
        throw new IllegalArgumentException("Duplicate channel entry found " + l + " " + r);
      },
      LinkedHashMap::new
    ));
    this.conditions = conditions;
    this.closeHandler = closeHandler;
  }

  @Override
  public void mark(ChannelUID channelUID) {
    WatchdogCondition condition = conditions.get(channelUID);
    if (condition != null) {
      condition.mark();
    }
  }

  @Override
  public boolean isTimedOut() {
    //return nextTimeout() < System.currentTimeMillis();
    return false;
  }

  @Override
  public ThingHandlerCallback getCallbackWrapper() {
    return callback;
  }

  @Override
  public long getTimeoutEventDelay() {
    //return delayMs;
    return 0L;
  }

  @Override
  public Map<WatchdogCondition, Long> getConditionIntervalMap() {
    return conditionIntervalMap;
  }

  @Override
  public void close() {
    closeHandler.accept(this);
  }

}
