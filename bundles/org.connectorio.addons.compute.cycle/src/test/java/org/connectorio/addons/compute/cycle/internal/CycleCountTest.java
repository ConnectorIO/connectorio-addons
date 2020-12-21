/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.compute.cycle.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.connectorio.addons.compute.cycle.internal.config.CounterChannelConfig;
import org.connectorio.addons.compute.cycle.internal.config.CycleCounterConfig;
import org.connectorio.addons.compute.cycle.internal.operation.CycleCount;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class CycleCountTest {

  final static String TRIGGER = "working";
  public static final long STEP_TIME = 1000L;

  public static ThingUID THING_UID = new ThingUID(CycleBindingConstants.THING_TYPE_CYCLE_COUNTER, "cnt1");
  public static final ChannelUID CHANNEL_COUNT = new ChannelUID(THING_UID, CycleBindingConstants.COUNT);

  @Mock
  ThingHandlerCallback callback;

  @Mock
  Supplier<Long> clock;

  CycleCounterConfig config = new CycleCounterConfig() {{
    trigger = TRIGGER;
  }};

  CounterChannelConfig counterConfig = new CounterChannelConfig() {{
    duration = 1;
    unit = TimeUnit.DAYS;
  }};

  @Test
  void testBasicCycle() {
    when(clock.get()).thenReturn(1000L)
      .thenReturn(11000L);

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleCount(clock, callback, CHANNEL_COUNT, counterConfig));
    receiver.accept(event(TRIGGER, OnOffType.ON));
    receiver.accept(event(TRIGGER, OnOffType.OFF));

    verify(callback).stateUpdated(CHANNEL_COUNT, new DecimalType(1L));
  }

  @Test
  void testInvalidCycle() {
    when(clock.get()).thenReturn(1000L);

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleCount(clock, callback, CHANNEL_COUNT, counterConfig));
    receiver.accept(event(TRIGGER, OnOffType.ON));
    receiver.accept(event(TRIGGER, OnOffType.ON));

    verify(clock, times(2)).get();
    verifyNoInteractions(callback);
  }

  @Test
  void testUnbalancedCycle() {
    when(clock.get()).thenReturn(1000L);

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleCount(clock, callback, CHANNEL_COUNT, counterConfig));
    receiver.accept(event(TRIGGER, OnOffType.OFF));
    receiver.accept(event(TRIGGER, OnOffType.ON));

    verify(clock, times(2)).get();
    verifyNoInteractions(callback);
  }

  @Test
  void testTruncatedCycle() {
    when(clock.get()).thenReturn(1000L);

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleCount(clock, callback, CHANNEL_COUNT, counterConfig));
    receiver.accept(event(TRIGGER, OnOffType.OFF));
    receiver.accept(event(TRIGGER, OnOffType.ON));
    receiver.accept(event(TRIGGER, OnOffType.OFF));

    verify(clock, times(2)).get();
    verify(callback).stateUpdated(CHANNEL_COUNT, new DecimalType(1L));
  }

  @Test
  void testMultipleCycles() {
    int cycles = 100;

    Supplier<Long> clock = mock(Supplier.class);
    OngoingStubbing<Long> timeStub = when(clock.get());
    for (int step = 0; step < cycles * 2; step++) {
      long time = step * STEP_TIME;
      timeStub = timeStub.thenReturn(time);
    }

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleCount(clock, callback, CHANNEL_COUNT, counterConfig));

    for (int step = 0; step < cycles; step++) {
      receiver.accept(event(TRIGGER, OnOffType.ON));
      receiver.accept(event(TRIGGER, OnOffType.OFF));
    }

    ArgumentCaptor<DecimalType> counter = ArgumentCaptor.forClass(DecimalType.class);
    verify(callback, times(cycles)).stateUpdated(eq(CHANNEL_COUNT), counter.capture());

    for (int step = 0; step < cycles; step++) {
      int cycle = step + 1;

      assertThat(counter.getAllValues().get(step))
        .isEqualTo(new DecimalType(cycle));
    }
  }

  private static ItemStateChangedEvent event(String itemName, State newState) {
    return event(itemName, newState, UnDefType.NULL);
  }

  private static ItemStateChangedEvent event(String itemName, State newState, UnDefType oldState) {
    ItemStateChangedEvent event = Mockito.mock(ItemStateChangedEvent.class, withSettings().lenient());
    when(event.getItemName()).thenReturn(itemName);
    when(event.getItemState()).thenReturn(newState);
    when(event.getOldItemState()).thenReturn(oldState);
    return event;
  }

}