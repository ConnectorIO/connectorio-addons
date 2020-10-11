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
package org.connectorio.binding.compute.cycle.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.connectorio.binding.compute.cycle.internal.CycleBindingConstants.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.connectorio.binding.compute.cycle.internal.config.CounterChannelConfig;
import org.connectorio.binding.compute.cycle.internal.config.CycleCounterConfig;
import org.connectorio.binding.compute.cycle.internal.operation.CycleCount;
import org.connectorio.binding.compute.cycle.internal.operation.CycleTime;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
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
import tec.uom.se.quantity.time.TimeQuantities;
import tec.uom.se.quantity.time.TimeUnitQuantity;

@ExtendWith(MockitoExtension.class)
class TriggerReceiverTest {

  final static String TRIGGER = "working";
  public static final long STEP_TIME = 1000L;

  public static ThingUID THING_UID = new ThingUID(THING_TYPE_CYCLE_COUNTER, "cnt1");
  public static final ChannelUID CHANNEL_TIME = new ChannelUID(THING_UID, TIME);

  @Mock
  ThingHandlerCallback callback;

  @Mock
  Supplier<Long> clock;

  CycleCounterConfig config = new CycleCounterConfig() {{
    trigger = TRIGGER;
  }};

  @Test
  void testBasicCycle() {
    when(clock.get()).thenReturn(1000L)
      .thenReturn(11000L);

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleTime(clock, callback, CHANNEL_TIME, config));
    receiver.accept(event(TRIGGER, OnOffType.ON));
    receiver.accept(event(TRIGGER, OnOffType.OFF));

    verify(callback).stateUpdated(CHANNEL_TIME, new QuantityType<>(10000L, TimeQuantities.MILLISECOND));
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
    receiver.addOperation(new CycleTime(clock, callback, CHANNEL_TIME, config));

    for (int step = 0; step < cycles; step++) {
      receiver.accept(event(TRIGGER, OnOffType.ON));
      receiver.accept(event(TRIGGER, OnOffType.OFF));
    }

    ArgumentCaptor<QuantityType> timer = ArgumentCaptor.forClass(QuantityType.class);
    verify(callback, times(cycles)).stateUpdated(eq(CHANNEL_TIME), timer.capture());

    for (int step = 0; step < cycles; step++) {
      int cycle = step + 1;

      assertThat(timer.getAllValues().get(step))
        .isEqualTo(new QuantityType<>(STEP_TIME, TimeQuantities.MILLISECOND));
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