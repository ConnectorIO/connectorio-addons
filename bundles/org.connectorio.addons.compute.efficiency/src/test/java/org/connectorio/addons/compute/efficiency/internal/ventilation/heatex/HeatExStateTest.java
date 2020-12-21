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
package org.connectorio.addons.compute.efficiency.internal.ventilation.heatex;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.function.Supplier;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeatExStateTest {

  final static String INTAKE = "intake";
  final static String SUPPLY = "supply";
  final static String EXTRACT = "extract";

  @Mock
  ThingHandlerCallback callback;

  @Mock
  ChannelUID channelUID;

  HeatExConfig config = new HeatExConfig() {{
    intakeTemperature = INTAKE;
    supplyTemperature = SUPPLY;
    extractTemperature = EXTRACT;
  }};

  @Test
  void testZeroEfficiency() {
    Supplier<Long> time = System::currentTimeMillis;

    HeatExState heatExState = new HeatExState(time, callback, channelUID, config);
    heatExState.accept(event(INTAKE, DecimalType.valueOf("50.0")));
    heatExState.accept(event(SUPPLY, DecimalType.valueOf("50.0")));
    heatExState.accept(event(EXTRACT, DecimalType.valueOf("50.0")));

    verify(callback).stateUpdated(channelUID, PercentType.ZERO);
  }

  @Test
  void testNormalEfficiency() {
    Supplier<Long> time = System::currentTimeMillis;

    HeatExState heatExState = new HeatExState(time, callback, channelUID, config);
    heatExState.accept(event(INTAKE, DecimalType.valueOf("10.0")));
    heatExState.accept(event(SUPPLY, DecimalType.valueOf("20.0")));
    heatExState.accept(event(EXTRACT, DecimalType.valueOf("30.0")));

    verify(callback).stateUpdated(channelUID, PercentType.valueOf("50"));
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