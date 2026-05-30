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

import static eu.chargetime.ocpp.model.core.ChargePointStatus.Available;
import static eu.chargetime.ocpp.model.core.ChargePointStatus.Charging;
import static eu.chargetime.ocpp.model.core.ChargePointStatus.Finishing;
import static eu.chargetime.ocpp.model.core.ChargePointStatus.Preparing;
import static eu.chargetime.ocpp.model.core.ChargePointStatus.SuspendedEV;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PhantomCycleDetectorTest {

  private final PhantomCycleDetector detector = new PhantomCycleDetector(60_000L, 2);

  @Test
  void firesAfterTwoPhantomCyclesInWindow() {
    // First phantom cycle: Available without ever charging.
    assertThat(detector.record(Available, 0)).isFalse();
    assertThat(detector.record(SuspendedEV, 100)).isFalse();
    assertThat(detector.record(Finishing, 200)).isFalse();
    // Second return to Available within the window — threshold reached.
    assertThat(detector.record(Available, 1_000)).isTrue();
  }

  @Test
  void doesNotFireWhenChargingHappened() {
    assertThat(detector.record(Available, 0)).isFalse();
    assertThat(detector.record(Preparing, 100)).isFalse();
    assertThat(detector.record(Charging, 200)).isFalse();
    assertThat(detector.record(Finishing, 300)).isFalse();
    // Return to Available after a real session — counter was cleared, no fire.
    assertThat(detector.record(Available, 400)).isFalse();
    assertThat(detector.pendingCycles()).isZero();
  }

  @Test
  void doesNotFireWhenCyclesAreOutsideWindow() {
    assertThat(detector.record(Available, 0)).isFalse();
    // Second Available is 70s later — first timestamp pruned, count stays at 1.
    assertThat(detector.record(Available, 70_000)).isFalse();
    assertThat(detector.pendingCycles()).isEqualTo(1);
  }

  @Test
  void clearsCounterAfterFiring() {
    detector.record(Available, 0);
    assertThat(detector.record(Available, 1_000)).isTrue();
    // A fresh run of cycles is needed before it fires again.
    assertThat(detector.record(Available, 2_000)).isFalse();
    assertThat(detector.record(Available, 3_000)).isTrue();
  }

  @Test
  void chargingMidFlapResetsTheCount() {
    assertThat(detector.record(Available, 0)).isFalse();
    assertThat(detector.record(Charging, 500)).isFalse();
    // Return to Available after a real session clears the pending count (legitimate teardown).
    assertThat(detector.record(Available, 1_000)).isFalse();
    assertThat(detector.pendingCycles()).isZero();
    // A subsequent lone phantom Available is then only count 1 — not enough to fire.
    assertThat(detector.record(Available, 2_000)).isFalse();
  }
}
