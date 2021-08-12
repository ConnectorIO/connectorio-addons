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
package org.connectorio.addons.norule;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import javax.measure.quantity.Energy;
import org.connectorio.addons.norule.internal.DefaultTriggerBuilderFactory;
import org.connectorio.addons.norule.internal.RuleLauncher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import tec.uom.se.quantity.Quantities;

@ExtendWith(MockitoExtension.class)
public class TestBasicRule {

  @Mock
  private ItemRegistry itemRegistry;

  @Mock
  private Item heatProduced;
  @Mock
  private Item energyConsumed;
  @Mock // generic item can receive state updates
  private GenericItem efficiency;

  @Test
  void checkBasicRule() {
    DefaultTriggerBuilderFactory triggerBuilderFactory = new DefaultTriggerBuilderFactory();

    ActualCopRule rule = new ActualCopRule(triggerBuilderFactory);

    RuleLauncher launcher = new RuleLauncher(itemRegistry);
    launcher.addRule(rule);

    when(itemRegistry.get("EnergyConsumed")).thenReturn(energyConsumed);
    when(energyConsumed.getStateAs(eq(QuantityType.class))).thenReturn(new QuantityType<>(10, Units.KILOWATT_HOUR));
    when(itemRegistry.get("HeatProduced")).thenReturn(heatProduced);
    when(heatProduced.getStateAs(eq(QuantityType.class))).thenReturn(new QuantityType<>(50, Units.KILOWATT_HOUR));
    when(itemRegistry.get("Efficiency")).thenReturn(efficiency);

    ItemStateChangedEvent event = create(ItemStateChangedEvent.class, new Class[] {
        String.class, String.class, String.class, State.class, State.class
      },
      "", "", "HeatProduced", QuantityType.valueOf("10 kWh"), QuantityType.valueOf("9 kWh")
    );
    launcher.receive(event);
    verify(efficiency).setState(new QuantityType<>(5, Units.ONE));
  }

  private <T> T create(Class<T> type, Class[] arguments, Object ... args) {
    try {
      Constructor<T> constructor = type.getDeclaredConstructor(arguments);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
