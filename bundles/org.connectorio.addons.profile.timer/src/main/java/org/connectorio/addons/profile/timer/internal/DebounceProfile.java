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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.connectorio.addons.profile.timer.DebounceManager;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DebounceProfile implements StateProfile {

  private final Logger logger = LoggerFactory.getLogger(DebounceProfile.class);
  private final boolean first;

  private AtomicReference<ScheduledFuture<?>> incomingFuture = new AtomicReference<>();
  private AtomicReference<ScheduledFuture<?>> outgoingFuture = new AtomicReference<>();

  private final ProfileCallback callback;
  private final DebounceManager debounceManager;
  private final long delay;
  private final TimeUnit unit;

  DebounceProfile(ProfileCallback callback, ProfileContext context, DebounceManager debounceManager) {
    this.callback = callback;

    this.delay = Long.parseLong(context.getConfiguration().get("delay").toString());
    this.unit = TimeUnit.valueOf((String) context.getConfiguration().get("unit"));
    this.first = Boolean.parseBoolean((String) context.getConfiguration().get("first"));
    this.debounceManager = debounceManager;
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return TimerProfiles.DEBOUNCE;
  }

  @Override
  public void onStateUpdateFromItem(State state) {
    if (state instanceof Command) {
      schedule(outgoingFuture, () -> { callback.handleCommand((Command) state); return state; });
      return;
    }
    logger.debug("Received item state {} was ignored since it could not be transformed to an command.", state);
  }

  @Override
  public void onCommandFromItem(Command command) {
    schedule(outgoingFuture, () -> { callback.handleCommand(command); return command; });
  }

  @Override
  public void onCommandFromHandler(Command command) {
    schedule(incomingFuture, () -> { callback.sendCommand(command); return command; });
  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    schedule(incomingFuture, () -> { callback.sendUpdate(state); return state; });
  }

  private void schedule(AtomicReference<ScheduledFuture<?>> scheduledUpdate, Callable<Type> action) {
    ScheduledFuture<?> scheduled = scheduledUpdate.get();
    if (scheduled != null) {
      if (first) {
        // ignore update
        return;
      }

      long actualDelay = scheduled.getDelay(unit);
      if (scheduled.cancel(false)) {
        // we successfully cancelled earlier update, reschedule new value with remaining time
        scheduledUpdate.set(debounceManager.schedule(wrapAction(scheduledUpdate, action), actualDelay, unit));
        return;
      }
    }

    scheduledUpdate.set(debounceManager.schedule(wrapAction(scheduledUpdate, action), delay, unit));
  }

  private Callable<Object> wrapAction(AtomicReference<ScheduledFuture<?>> future, Callable<Type> action) {
    return new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        Type result = null;
        try {
          result = action.call();
          return result;
        } finally {
          logger.debug("Executed action with result {}", result);
          future.set(null);
        }
      }
    };
  }

}
