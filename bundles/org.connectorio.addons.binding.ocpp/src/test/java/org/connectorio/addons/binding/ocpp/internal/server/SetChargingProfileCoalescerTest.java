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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class SetChargingProfileCoalescerTest {

  /** Records each wireValue sent and hands back a future the test completes by hand. */
  private static class RecordingSender implements SetChargingProfileCoalescer.Sender {
    final List<Integer> sent = new ArrayList<>();
    final List<CompletableFuture<Object>> futures = new ArrayList<>();

    @Override
    public CompletableFuture<Object> send(int wireValue) {
      sent.add(wireValue);
      CompletableFuture<Object> future = new CompletableFuture<>();
      futures.add(future);
      return future;
    }

    void complete(int index) {
      futures.get(index).complete(null);
    }
  }

  /** Runs scheduled drains immediately so timing is deterministic. */
  private static final SetChargingProfileCoalescer.DelayedExecutor IMMEDIATE =
      (task, delayMs) -> task.run();

  @Test
  void sendsImmediatelyWhenIdle() {
    RecordingSender sender = new RecordingSender();
    SetChargingProfileCoalescer coalescer =
        new SetChargingProfileCoalescer(500, () -> 0L, sender, IMMEDIATE);

    coalescer.submit(10);

    assertThat(sender.sent).containsExactly(10);
  }

  @Test
  void coalescesWhileInFlightAndSendsOnlyLatest() {
    RecordingSender sender = new RecordingSender();
    long[] now = {0L};
    SetChargingProfileCoalescer coalescer =
        new SetChargingProfileCoalescer(0, () -> now[0], sender, IMMEDIATE);

    coalescer.submit(10);     // sent immediately, in flight
    coalescer.submit(12);     // coalesced into pending
    coalescer.submit(14);     // overwrites pending — 12 never reaches the wire
    assertThat(sender.sent).containsExactly(10);

    sender.complete(0);       // in-flight settles → drains pending (14)
    assertThat(sender.sent).containsExactly(10, 14);
  }

  @Test
  void doesNotResendWhenPendingEqualsInFlight() {
    RecordingSender sender = new RecordingSender();
    SetChargingProfileCoalescer coalescer =
        new SetChargingProfileCoalescer(0, () -> 0L, sender, IMMEDIATE);

    coalescer.submit(10);
    coalescer.submit(10);     // same value while in flight — nothing new to send
    sender.complete(0);

    assertThat(sender.sent).containsExactly(10);
  }

  @Test
  void sequentialIdleSendsEachGoOut() {
    RecordingSender sender = new RecordingSender();
    SetChargingProfileCoalescer coalescer =
        new SetChargingProfileCoalescer(0, () -> 0L, sender, IMMEDIATE);

    coalescer.submit(10);
    sender.complete(0);       // settles, nothing pending
    coalescer.submit(16);     // idle again → sends
    sender.complete(1);

    assertThat(sender.sent).containsExactly(10, 16);
  }

  @Test
  void respectsMinIntervalBeforeDraining() {
    RecordingSender sender = new RecordingSender();
    long[] now = {0L};
    List<Long> scheduledDelays = new ArrayList<>();
    SetChargingProfileCoalescer.DelayedExecutor capturing = (task, delayMs) -> {
      scheduledDelays.add(delayMs);
      task.run();
    };
    SetChargingProfileCoalescer coalescer =
        new SetChargingProfileCoalescer(500, () -> now[0], sender, capturing);

    coalescer.submit(10);     // sent at t=0
    coalescer.submit(14);     // pending
    now[0] = 200L;            // 200ms elapsed when it settles
    sender.complete(0);

    // Drain scheduled with the remaining 300ms of the 500ms minimum interval.
    assertThat(scheduledDelays).containsExactly(300L);
    assertThat(sender.sent).containsExactly(10, 14);
  }
}
