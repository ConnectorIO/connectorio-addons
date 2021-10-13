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
package org.connectorio.addons.profile.timer.internal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.connectorio.addons.profile.timer.DebounceManager;
import org.connectorio.chrono.Period;
import org.connectorio.chrono.shared.FuturePeriodCalculator;
import org.connectorio.chrono.shared.PastPeriodCalculator;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PeriodicProfile implements StateProfile {

  private final Logger logger = LoggerFactory.getLogger(PeriodicProfile.class);

  private final AtomicReference<Type> incoming = new AtomicReference<>();
  private final AtomicReference<Type> outgoing = new AtomicReference<>();

  private ScheduledFuture<?> incomingFuture;
  private ScheduledFuture<?> outgoingFuture;

  private final ProfileCallback callback;
  private final DebounceManager debounceManager;
  private final long amount;
  private final Period period;
  private final boolean refresh;

  PeriodicProfile(ProfileCallback callback, ProfileContext context, DebounceManager debounceManager) {
    this.callback = callback;
    this.debounceManager = debounceManager;

    this.amount = Long.parseLong(context.getConfiguration().get("amount").toString());
    this.period = Period.valueOf((String) context.getConfiguration().get("period"));
    this.refresh = Boolean.parseBoolean((String) context.getConfiguration().get("refresh"));

    Clock clock = Clock.systemUTC();
    Instant now = clock.instant();
    FuturePeriodCalculator future = new FuturePeriodCalculator(Clock.fixed(now, clock.getZone()), period);
    PastPeriodCalculator past = new PastPeriodCalculator(Clock.fixed(now, clock.getZone()), period);
    ZonedDateTime futureExecTime = future.calculate();
    ZonedDateTime pastExecTime = past.calculate();

    long initialDelay = Duration.between(futureExecTime.toInstant(), now).toMillis();
    long delay = Duration.between(futureExecTime, pastExecTime).toMillis() * amount;

    incomingFuture = debounceManager.scheduleFixed(new PeriodicRunnable(callback, incoming, true, refresh),
      initialDelay, delay, TimeUnit.MILLISECONDS);
    outgoingFuture = debounceManager.scheduleFixed(new PeriodicRunnable(callback, outgoing, false, refresh),
      initialDelay, delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return TimerProfiles.PERIODIC;
  }

  @Override
  public void onStateUpdateFromItem(State state) {
    if (state instanceof Command) {
      outgoing.set(state);
      return;
    }
    logger.debug("Received item state {} was ignored since it could not be transformed to an command.", state);
  }

  @Override
  public void onCommandFromItem(Command command) {
    outgoing.set(command);
  }

  @Override
  public void onCommandFromHandler(Command command) {
    incoming.set(command);
  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    incoming.set(state);
  }

  static class PeriodicRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(PeriodicRunnable.class);
    private final ProfileCallback callback;
    private final AtomicReference<Type> reference;
    private final boolean incoming;
    private final boolean refresh;

    PeriodicRunnable(ProfileCallback callback, AtomicReference<Type> reference, boolean incoming, boolean refresh) {
      this.callback = callback;
      this.reference = reference;
      this.incoming = incoming;
      this.refresh = refresh;
    }

    @Override
    public void run() {
      Type value = reference.get();
      if (value == null) {
        return;
      }

      if (value == RefreshType.REFRESH) {
        // hold refresh commands, we can supply them!
        return;
      }

      if (incoming) {
        if (value instanceof State) {
          callback.sendUpdate((State) value);
        } else if (value instanceof Command) {
          callback.sendCommand((Command) value);
        } else {
          logger.error("Unsupported value received {} from handler. It will not be forwarded to framework.", value);
        }
      } else {
        if (value instanceof Command) {
          callback.handleCommand((Command) value);
        } else {
          logger.error("Unsupported value received {} from framework. It will not be forwarded to handler.", value);
        }
      }

      // reset state
      reference.set(null);

      if (refresh) {
        if (incoming) {
          callback.sendCommand(RefreshType.REFRESH);
        } else {
          callback.handleCommand(RefreshType.REFRESH);
        }
      }

      return;
    }
  }


}
