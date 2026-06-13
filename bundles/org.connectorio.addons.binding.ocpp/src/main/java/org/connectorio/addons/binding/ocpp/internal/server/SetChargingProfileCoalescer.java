/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.ocpp.internal.server;

import java.util.concurrent.CompletionStage;
import java.util.function.LongSupplier;

/**
 * Coalesces rapid charge-limit changes into at most one in-flight SetChargingProfile CALL plus one
 * pending value, so a fast caller (a solar/EMS loop adjusting amperage many times a second) cannot
 * flood the charger's OCPP queue.
 *
 * <p>While a SetChargingProfile is in flight, further {@link #submit(int)} calls only overwrite the
 * pending value — they do not enqueue more CALLs. When the in-flight CALL settles, if the pending
 * value still differs from what was just sent, exactly one follow-up CALL is scheduled, no sooner
 * than {@code minIntervalMs} after the previous send. This keeps Phoenix Contact CHARX SEC-3xxx
 * (whose OCPP queue is slow) from backing up under load while still converging on the latest
 * desired current.
 *
 * <p>The class is transport-agnostic: {@code sender} performs the actual CALL and returns its
 * future; {@code delayedExecutor} schedules the drain; {@code clock} supplies the current time
 * (injected for testability).
 */
public class SetChargingProfileCoalescer {

  @FunctionalInterface
  public interface Sender {
    CompletionStage<?> send(int wireValue);
  }

  @FunctionalInterface
  public interface DelayedExecutor {
    void schedule(Runnable task, long delayMs);
  }

  private final long minIntervalMs;
  private final LongSupplier clock;
  private final Sender sender;
  private final DelayedExecutor delayedExecutor;

  private Integer inFlight;
  private Integer pending;
  private long lastSentAtMs;
  private boolean drainScheduled;

  public SetChargingProfileCoalescer(long minIntervalMs, LongSupplier clock, Sender sender,
      DelayedExecutor delayedExecutor) {
    this.minIntervalMs = minIntervalMs;
    this.clock = clock;
    this.sender = sender;
    this.delayedExecutor = delayedExecutor;
  }

  /** Request that {@code wireValue} reach the charger, coalescing with any in-flight or pending send. */
  public synchronized void submit(int wireValue) {
    if (inFlight != null) {
      pending = wireValue;
      return;
    }
    sendNow(wireValue);
  }

  private void sendNow(int wireValue) {
    inFlight = wireValue;
    lastSentAtMs = clock.getAsLong();
    sender.send(wireValue).whenComplete((result, error) -> settled());
  }

  private synchronized void settled() {
    Integer wasInFlight = inFlight;
    inFlight = null;
    if (pending == null || pending.equals(wasInFlight)) {
      pending = null;
      return;
    }
    if (drainScheduled) {
      return;
    }
    long elapsed = clock.getAsLong() - lastSentAtMs;
    long delay = Math.max(0L, minIntervalMs - elapsed);
    drainScheduled = true;
    delayedExecutor.schedule(this::drain, delay);
  }

  private synchronized void drain() {
    drainScheduled = false;
    if (pending == null) {
      return;
    }
    int next = pending;
    pending = null;
    sendNow(next);
  }
}
