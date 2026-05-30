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
import static eu.chargetime.ocpp.model.core.ChargePointStatus.Faulted;
import static eu.chargetime.ocpp.model.core.ChargePointStatus.Finishing;
import static eu.chargetime.ocpp.model.core.ChargePointStatus.SuspendedEVSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.connectorio.addons.binding.ocpp.internal.server.StuckStateWatchdog.Action.CHANGE_AVAILABILITY;
import static org.connectorio.addons.binding.ocpp.internal.server.StuckStateWatchdog.Action.NONE;
import static org.connectorio.addons.binding.ocpp.internal.server.StuckStateWatchdog.Action.TRIGGER_STATUS;
import static org.connectorio.addons.binding.ocpp.internal.server.StuckStateWatchdog.Action.UNLOCK;

import org.junit.jupiter.api.Test;

class StuckStateWatchdogTest {

  // nudge=20s, availability=40s, unlock=60s, teardownGrace=300s, teardownUnlock=180s (defaults)
  private final StuckStateWatchdog watchdog = new StuckStateWatchdog();

  @Test
  void healthyStatesNeverEscalate() {
    watchdog.onStatus(Available, 0);
    assertThat(watchdog.evaluate(100_000)).isEqualTo(NONE);
    watchdog.onStatus(Charging, 100_000);
    assertThat(watchdog.evaluate(200_000)).isEqualTo(NONE);
  }

  @Test
  void faultedEscalatesThroughAllThreeTiers() {
    watchdog.onStatus(Faulted, 0);
    assertThat(watchdog.evaluate(10_000)).isEqualTo(NONE);            // <20s
    assertThat(watchdog.evaluate(20_000)).isEqualTo(TRIGGER_STATUS);  // 20s
    assertThat(watchdog.evaluate(30_000)).isEqualTo(NONE);            // already nudged
    assertThat(watchdog.evaluate(40_000)).isEqualTo(CHANGE_AVAILABILITY); // 40s
    assertThat(watchdog.evaluate(60_000)).isEqualTo(UNLOCK);          // 60s
    assertThat(watchdog.evaluate(80_000)).isEqualTo(NONE);            // nothing left
  }

  @Test
  void legitimateTeardownSkipsAvailabilityAndDefersUnlock() {
    // A real session: Charging then Finishing 1s later.
    watchdog.onStatus(Charging, 0);
    watchdog.onStatus(Finishing, 1_000);

    // Nudge still fires at 20s into Finishing.
    assertThat(watchdog.evaluate(21_000)).isEqualTo(TRIGGER_STATUS);
    // ChangeAvailability is skipped during teardown — 40s in, nothing.
    assertThat(watchdog.evaluate(41_000)).isEqualTo(NONE);
    // Still nothing at the normal 60s unlock point.
    assertThat(watchdog.evaluate(61_000)).isEqualTo(NONE);
    // Unlock only after the extended teardown window (180s into Finishing → t≈181s).
    assertThat(watchdog.evaluate(181_000)).isEqualTo(UNLOCK);
  }

  @Test
  void phantomFinishingWithoutPriorChargingEscalatesNormally() {
    // Finishing with no recent Charging is not a legitimate teardown.
    watchdog.onStatus(Finishing, 0);
    assertThat(watchdog.evaluate(20_000)).isEqualTo(TRIGGER_STATUS);
    assertThat(watchdog.evaluate(40_000)).isEqualTo(CHANGE_AVAILABILITY);
    assertThat(watchdog.evaluate(60_000)).isEqualTo(UNLOCK);
  }

  @Test
  void chargingOlderThanGraceIsNotTeardown() {
    watchdog.onStatus(Charging, 0);
    // Finishing enters far later — last charging is now older than the 300s grace.
    watchdog.onStatus(Finishing, 400_000);
    assertThat(watchdog.evaluate(440_000)).isEqualTo(CHANGE_AVAILABILITY); // 40s in, treated as stuck
  }

  @Test
  void statusChangeResetsTheLadder() {
    watchdog.onStatus(Faulted, 0);
    assertThat(watchdog.evaluate(20_000)).isEqualTo(TRIGGER_STATUS);
    // Charger recovers to Available, then faults again — ladder restarts.
    watchdog.onStatus(Available, 25_000);
    assertThat(watchdog.evaluate(30_000)).isEqualTo(NONE);
    watchdog.onStatus(Faulted, 35_000);
    assertThat(watchdog.evaluate(50_000)).isEqualTo(NONE);            // only 15s into the new Faulted
    assertThat(watchdog.evaluate(55_000)).isEqualTo(TRIGGER_STATUS);  // 20s into the new Faulted
  }

  @Test
  void suspendedEvseIsNotTreatedAsStuck() {
    // SuspendedEVSE is typically a deliberate pause (chargeLimit=0) — never auto-recovered.
    watchdog.onStatus(SuspendedEVSE, 0);
    assertThat(watchdog.evaluate(120_000)).isEqualTo(NONE);
  }
}
