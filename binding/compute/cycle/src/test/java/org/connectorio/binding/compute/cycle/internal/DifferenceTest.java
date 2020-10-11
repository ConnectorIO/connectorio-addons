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

import org.connectorio.binding.compute.cycle.internal.config.CycleCounterConfig;
import org.connectorio.binding.compute.cycle.internal.config.DifferenceChannelConfig;
import org.connectorio.binding.compute.cycle.internal.operation.CycleDifference;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.SmartHomeUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DifferenceTest {

  final static String TRIGGER = "valve-open";
  final static String MEASURE = "gas-meter";

  public static ThingUID THING_UID = new ThingUID(THING_TYPE_CYCLE_COUNTER, "cnt-x");
  public static final ChannelUID DIFFERENCE_CHANNEL = new ChannelUID(THING_UID, "difference-1");

  @Mock
  ThingHandlerCallback callback;

  @Mock
  ItemRegistry registry;

  CycleCounterConfig config = new CycleCounterConfig() {{
    trigger = TRIGGER;
  }};

  DifferenceChannelConfig channelConfig = new DifferenceChannelConfig() {{
    measure = MEASURE;
  }};

  @Test
  void testBasicCycle() {
    Item item = mock(Item.class);
    when(item.getStateAs(QuantityType.class)).thenReturn(QuantityType.valueOf(100.0, SmartHomeUnits.WATT_HOUR))
      .thenReturn(QuantityType.valueOf(200.01, SmartHomeUnits.WATT_HOUR));

    try {
      when(registry.getItem(MEASURE)).thenReturn(item);
    } catch (ItemNotFoundException e) {
      // not relevant
    }

    TriggerReceiver receiver = new TriggerReceiver();
    receiver.addOperation(new CycleDifference(registry, callback, DIFFERENCE_CHANNEL, channelConfig));
    receiver.accept(event(TRIGGER, OnOffType.ON));
    receiver.accept(event(TRIGGER, OnOffType.OFF));

    verify(callback).stateUpdated(DIFFERENCE_CHANNEL, QuantityType.valueOf(100.01, SmartHomeUnits.WATT_HOUR));
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