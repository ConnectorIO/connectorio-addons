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
package org.connectorio.addons.automation.calculation.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.connectorio.addons.automation.calculation.CalculationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.internal.ActionImpl;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

@ExtendWith(MockitoExtension.class)
class VolatileCalculationActionHandlerTest {

  public static final String ITEM_ENERGY_READING = "energyReading";
  public static final String ITEM_ENERGY_USE = "energyUse";

  private VolatileCalculationActionHandler handler;

  @Mock
  private ItemRegistry itemRegistry;
  @Mock
  private EventPublisher eventPublisher;
  @Mock
  private Item item;

  @BeforeEach
  void setup() {
    Map<String, Object> config = new HashMap<>();
    config.put("input", ITEM_ENERGY_READING);
    config.put("output", ITEM_ENERGY_USE);

    Configuration cfg = new Configuration(config);
    this.handler = new VolatileCalculationActionHandler(
      new ActionImpl("test", "test", cfg, null, null, null),
      eventPublisher, itemRegistry
    );
  }

  @Test
  void testCalculationLogic() throws Exception {
    when(itemRegistry.getItem(eq(ITEM_ENERGY_READING))).thenReturn(item);
    when(item.getState()).thenReturn(new QuantityType<>(100.0, Units.KILOWATT_HOUR));

    Map<String, Object> context = new HashMap<>();
    Instant firstTrigger = createInstant(2020, 12, 12, 1, 0, 0);
    context.put("1." + CalculationConstants.TRIGGER_TIME, firstTrigger);

    Map<String, Object> result = handler.execute(context);
    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, UnDefType.NULL);

    Instant secondTrigger = createInstant(2020, 12, 12, 2, 0, 0);
    context.put("1." + CalculationConstants.TRIGGER_TIME, secondTrigger);
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, firstTrigger);
    result = handler.execute(context);
    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(0.0, Units.KILOWATT_HOUR));

    when(item.getState()).thenReturn(new QuantityType<>(101.01, Units.KILOWATT_HOUR));
    context.put("1." + CalculationConstants.TRIGGER_TIME, createInstant(2020, 12, 12, 3, 0, 0));
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, secondTrigger);
    result = handler.execute(context);

    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(1.01, Units.KILOWATT_HOUR));
  }

//  @Test
//  void testMultipleCycles() {
//    int cycles = 100;
//
//    Supplier<Long> clock = mock(Supplier.class);
//    OngoingStubbing<Long> timeStub = when(clock.get());
//    for (int step = 0; step < cycles * 2; step++) {
//      long time = step * STEP_TIME;
//      timeStub = timeStub.thenReturn(time);
//    }
//
//    TriggerReceiver receiver = new TriggerReceiver();
//    receiver.addOperation(new CycleTime(clock, callback, CHANNEL_TIME, config));
//
//    for (int step = 0; step < cycles; step++) {
//      receiver.accept(event(TRIGGER, OnOffType.ON));
//      receiver.accept(event(TRIGGER, OnOffType.OFF));
//    }
//
//    ArgumentCaptor<QuantityType> timer = ArgumentCaptor.forClass(QuantityType.class);
//    verify(callback, times(cycles)).stateUpdated(eq(CHANNEL_TIME), timer.capture());
//
//    for (int step = 0; step < cycles; step++) {
//      int cycle = step + 1;
//
//      assertThat(timer.getAllValues().get(step))
//        .isEqualTo(new QuantityType<>(STEP_TIME, TimeQuantities.MILLISECOND));
//    }
//  }
//
//  private static ItemStateChangedEvent event(String itemName, State newState) {
//    return event(itemName, newState, UnDefType.NULL);
//  }
//
//  private static ItemStateChangedEvent event(String itemName, State newState, UnDefType oldState) {
//    ItemStateChangedEvent event = Mockito.mock(ItemStateChangedEvent.class, withSettings().lenient());
//    when(event.getItemName()).thenReturn(itemName);
//    when(event.getItemState()).thenReturn(newState);
//    when(event.getOldItemState()).thenReturn(oldState);
//    return event;
//  }

  private static Instant createInstant(int year, int month, int day, int hour, int minute, int second) {
    return LocalDateTime.of(year, month, day, hour, minute, second).withNano(0).toInstant(ZoneOffset.UTC);
  }

}