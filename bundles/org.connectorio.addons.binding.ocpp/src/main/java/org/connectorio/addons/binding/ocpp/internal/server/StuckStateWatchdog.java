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

/**
 * Decides when and how to recover a connector that is stuck. Some chargers hang in
 * {@link ChargePointStatus#Finishing} (a transaction that never fully tears down) or
 * {@link ChargePointStatus#Faulted}, blocking new sessions until someone physically intervenes.
 * Phoenix Contact CHARX SEC-3xxx is prone to the Finishing hang.
 *
 * <p>Feed every StatusNotification into {@link #onStatus(ChargePointStatus, long)} and call
 * {@link #evaluate(long)} on a timer. While the connector sits in a stuck state, {@code evaluate}
 * escalates once per tier:
 *
 * <ol>
 *   <li>{@value #DEFAULT_NUDGE_MS} ms → {@link Action#TRIGGER_STATUS} (a cheap state refresh);</li>
 *   <li>{@value #DEFAULT_AVAILABILITY_MS} ms → {@link Action#CHANGE_AVAILABILITY} (force a firmware
 *       state-machine reset);</li>
 *   <li>{@value #DEFAULT_UNLOCK_MS} ms → {@link Action#UNLOCK} (terminate a phantom transaction).</li>
 * </ol>
 *
 * <p>A <em>legitimate teardown</em> — a {@code Finishing} that follows a real Charging session
 * within {@value #DEFAULT_TEARDOWN_GRACE_MS} ms — is treated gently: the {@code ChangeAvailability}
 * cycle is skipped (it would break the charger's natural wind-down) and the connector is given until
 * {@value #DEFAULT_TEARDOWN_UNLOCK_MS} ms before any unlock. Normal teardowns (Wallbox Pulsar Plus
 * can take 30–90 s) therefore never trigger an aggressive reset.
 */
public class StuckStateWatchdog {

  public enum Action {
    NONE,
    TRIGGER_STATUS,
    CHANGE_AVAILABILITY,
    UNLOCK
  }

  public static final long DEFAULT_NUDGE_MS = 20_000L;
  public static final long DEFAULT_AVAILABILITY_MS = 40_000L;
  public static final long DEFAULT_UNLOCK_MS = 60_000L;
  public static final long DEFAULT_TEARDOWN_GRACE_MS = 300_000L;
  public static final long DEFAULT_TEARDOWN_UNLOCK_MS = 180_000L;

  private final long nudgeMs;
  private final long availabilityMs;
  private final long unlockMs;
  private final long teardownGraceMs;
  private final long teardownUnlockMs;

  private ChargePointStatus currentStatus;
  private long stateEnteredMs;
  private long lastChargingMs = -1L;
  private int firedTier;

  public StuckStateWatchdog() {
    this(DEFAULT_NUDGE_MS, DEFAULT_AVAILABILITY_MS, DEFAULT_UNLOCK_MS,
        DEFAULT_TEARDOWN_GRACE_MS, DEFAULT_TEARDOWN_UNLOCK_MS);
  }

  public StuckStateWatchdog(long nudgeMs, long availabilityMs, long unlockMs,
      long teardownGraceMs, long teardownUnlockMs) {
    this.nudgeMs = nudgeMs;
    this.availabilityMs = availabilityMs;
    this.unlockMs = unlockMs;
    this.teardownGraceMs = teardownGraceMs;
    this.teardownUnlockMs = teardownUnlockMs;
  }

  /** Record a status sample. Resets the escalation ladder whenever the status actually changes. */
  public synchronized void onStatus(ChargePointStatus status, long nowMs) {
    if (status == ChargePointStatus.Charging) {
      lastChargingMs = nowMs;
    }
    if (status != currentStatus) {
      currentStatus = status;
      stateEnteredMs = nowMs;
      firedTier = 0;
    }
  }

  /** @return the next recovery action to perform, or {@link Action#NONE} if nothing is due yet. */
  public synchronized Action evaluate(long nowMs) {
    if (currentStatus == null || !isStuckCandidate(currentStatus)) {
      firedTier = 0;
      return Action.NONE;
    }
    long inState = nowMs - stateEnteredMs;
    int target = 0;
    if (isLegitimateTeardown(nowMs)) {
      // Gentle path: nudge, then a much-delayed unlock. Never ChangeAvailability mid-teardown.
      if (inState >= nudgeMs) {
        target = 1;
      }
      if (inState >= teardownUnlockMs) {
        target = 3;
      }
    } else {
      if (inState >= nudgeMs) {
        target = 1;
      }
      if (inState >= availabilityMs) {
        target = 2;
      }
      if (inState >= unlockMs) {
        target = 3;
      }
    }
    if (target > firedTier) {
      firedTier = target;
      return actionForTier(target);
    }
    return Action.NONE;
  }

  private boolean isLegitimateTeardown(long nowMs) {
    return currentStatus == ChargePointStatus.Finishing
        && lastChargingMs >= 0
        && (nowMs - lastChargingMs) < teardownGraceMs;
  }

  private static boolean isStuckCandidate(ChargePointStatus status) {
    return status == ChargePointStatus.Finishing || status == ChargePointStatus.Faulted;
  }

  private static Action actionForTier(int tier) {
    switch (tier) {
      case 1:
        return Action.TRIGGER_STATUS;
      case 2:
        return Action.CHANGE_AVAILABILITY;
      case 3:
        return Action.UNLOCK;
      default:
        return Action.NONE;
    }
  }
}
