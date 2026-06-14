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
  void firesAfterTwoGenuineCyclesInWindow() {
    // A phantom cycle flaps Available -> SuspendedEV -> Finishing -> Available without charging.
    assertThat(detector.record(Available, 0)).isFalse(); // initial state, not yet a return
    assertThat(detector.record(SuspendedEV, 100)).isFalse();
    assertThat(detector.record(Finishing, 200)).isFalse();
    assertThat(detector.record(Available, 300)).isFalse(); // genuine return #1
    assertThat(detector.record(SuspendedEV, 400)).isFalse();
    assertThat(detector.record(Finishing, 500)).isFalse();
    assertThat(detector.record(Available, 600)).isTrue(); // genuine return #2 -> threshold reached
  }

  @Test
  void doesNotFireOnRepeatedAvailableReports() {
    // A charger re-announcing Available on (re)connect or after a TriggerMessage reports the same
    // state repeatedly. Those duplicates are NOT cycles and must not trip the detector — this is the
    // connect/reconnect status burst that otherwise fires it at startup.
    assertThat(detector.record(Available, 0)).isFalse();
    assertThat(detector.record(Available, 1_000)).isFalse();
    assertThat(detector.record(Available, 2_000)).isFalse();
    assertThat(detector.record(Available, 3_000)).isFalse();
    assertThat(detector.pendingCycles()).isZero();
  }

  @Test
  void doesNotFireWhenChargingHappened() {
    assertThat(detector.record(Available, 0)).isFalse();
    assertThat(detector.record(Preparing, 100)).isFalse();
    assertThat(detector.record(Charging, 200)).isFalse();
    assertThat(detector.record(Finishing, 300)).isFalse();
    // Return to Available after a real session — counter cleared, no fire.
    assertThat(detector.record(Available, 400)).isFalse();
    assertThat(detector.pendingCycles()).isZero();
  }

  @Test
  void doesNotFireWhenGenuineCyclesAreOutsideWindow() {
    assertThat(detector.record(Available, 0)).isFalse(); // initial
    assertThat(detector.record(Preparing, 10)).isFalse();
    assertThat(detector.record(Available, 100)).isFalse(); // genuine return #1
    assertThat(detector.record(Preparing, 70_000)).isFalse();
    // Genuine return #2 is 70s after the first — the first timestamp is pruned, count stays at 1.
    assertThat(detector.record(Available, 70_100)).isFalse();
    assertThat(detector.pendingCycles()).isEqualTo(1);
  }

  @Test
  void clearsCounterAfterFiring() {
    detector.record(Available, 0);
    detector.record(Preparing, 10);
    detector.record(Available, 100); // return #1
    detector.record(Preparing, 110);
    assertThat(detector.record(Available, 200)).isTrue(); // return #2 -> fire, counter cleared
    // A fresh run of cycles is needed before it fires again.
    detector.record(Preparing, 210);
    assertThat(detector.record(Available, 300)).isFalse(); // return #1 of the new run
    detector.record(Preparing, 310);
    assertThat(detector.record(Available, 400)).isTrue(); // return #2 -> fire again
  }

  @Test
  void chargingMidFlapResetsTheCount() {
    assertThat(detector.record(Available, 0)).isFalse();
    assertThat(detector.record(Preparing, 50)).isFalse();
    assertThat(detector.record(Available, 100)).isFalse(); // genuine return #1
    assertThat(detector.record(Charging, 150)).isFalse();
    // Return to Available after a real session clears the pending count (legitimate teardown).
    assertThat(detector.record(Available, 200)).isFalse();
    assertThat(detector.pendingCycles()).isZero();
    // A subsequent lone genuine return is then only count 1 — not enough to fire.
    assertThat(detector.record(Preparing, 250)).isFalse();
    assertThat(detector.record(Available, 300)).isFalse();
  }
}
