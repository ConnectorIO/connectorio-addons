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
package org.connectorio.addons.communication.watchdog;

import java.time.Duration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Watchdog builder allows to specify settings for watchdog and channels which should be polled on
 * cyclic basis.
 *
 * All settings specified in builder will impact behavior of Watchdog itself.
 */
public interface WatchdogBuilder {

  WatchdogBuilder withChannel(ChannelUID channel, long timeoutPeriodMs);
  WatchdogBuilder withChannel(ChannelUID channel, Duration duration);

  WatchdogBuilder withChannel(ChannelUID channel, WatchdogCondition condition);

  WatchdogBuilder withTimeoutDelay(long timeout);

  WatchdogBuilder withTimeoutMultiplier(int multiplier);

  Watchdog build(ThingHandlerCallback callback, WatchdogListener listener);

}