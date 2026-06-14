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

import eu.chargetime.ocpp.model.core.ChargePointStatus;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Detects phantom connector cycles. Some charger firmwares (observed on Wallbox Copper SB)
 * hallucinate plug-in events: the connector flaps Available → SuspendedEV → Finishing → Available
 * with no cable, never reaching Charging. A real session always passes through Charging.
 *
 * <p>Feed every StatusNotification into {@link #record(ChargePointStatus, long)}; it returns
 * {@code true} when the connector has returned to Available {@code threshold}+ times within
 * {@code windowMs} without an intervening Charging state, which is the cue to reset the connector's
 * state machine (e.g. via ChangeAvailability Inoperative → Operative).
 */
public class PhantomCycleDetector {

  private final long windowMs;
  private final int threshold;
  private final Deque<Long> availableTimestamps = new ArrayDeque<>();
  private boolean chargingSinceAvailable;
  private ChargePointStatus lastStatus;

  public PhantomCycleDetector(long windowMs, int threshold) {
    this.windowMs = windowMs;
    this.threshold = threshold;
  }

  /**
   * Record a status sample.
   *
   * @return {@code true} if this sample completes a phantom-cycle threshold and a reset should fire.
   *     The internal counter is cleared when this returns {@code true}, so a subsequent reset needs
   *     a fresh run of cycles.
   */
  public synchronized boolean record(ChargePointStatus status, long nowMs) {
    ChargePointStatus previous = lastStatus;
    lastStatus = status;
    if (status == ChargePointStatus.Charging) {
      chargingSinceAvailable = true;
      return false;
    }
    if (status != ChargePointStatus.Available) {
      return false;
    }
    if (chargingSinceAvailable) {
      // A real session ran since the last Available — not a phantom cycle.
      chargingSinceAvailable = false;
      availableTimestamps.clear();
      return false;
    }
    if (previous == null || previous == ChargePointStatus.Available) {
      // Only a genuine return counts: the connector must have actually left Available and come
      // back. A charger re-announcing Available on (re)connect or in response to a TriggerMessage
      // reports the same state repeatedly — those duplicates are not cycles and must not trip the
      // detector (otherwise the connect/reconnect status burst fires it at startup).
      return false;
    }
    availableTimestamps.addLast(nowMs);
    while (!availableTimestamps.isEmpty() && nowMs - availableTimestamps.peekFirst() > windowMs) {
      availableTimestamps.removeFirst();
    }
    if (availableTimestamps.size() >= threshold) {
      availableTimestamps.clear();
      return true;
    }
    return false;
  }

  public synchronized int pendingCycles() {
    return availableTimestamps.size();
  }
}
