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
package org.connectorio.addons.norule.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import javax.measure.quantity.Energy;
import org.connectorio.addons.norule.ActualCopRule;
import org.connectorio.addons.norule.BlockingRule;
import org.connectorio.addons.norule.internal.action.DefaultThingActionsRegistry;
import org.connectorio.addons.test.ItemMutation;
import org.connectorio.addons.test.StubEventBuilder;
import org.connectorio.addons.test.StubItemBuilder;
import org.connectorio.addons.test.TestingItemRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.internal.service.ReadyServiceImpl;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingRegistry;

@ExtendWith(MockitoExtension.class)
public class TestBasicRule {

  @Mock
  private ThingRegistry thingRegistry;

  private NumberItem heatProduced = StubItemBuilder.createQuantity(Energy.class, ActualCopRule.HEAT_PRODUCED).build();
  private NumberItem energyConsumed = StubItemBuilder.createQuantity(Energy.class, ActualCopRule.ENERGY_CONSUMED).build();
  private NumberItem efficiency = StubItemBuilder.createNumber(ActualCopRule.EFFICIENCY).build();

  @Test
  void checkBasicRule() throws InterruptedException {
    DefaultTriggerBuilderFactory triggerBuilderFactory = new DefaultTriggerBuilderFactory();

    BlockingRule rule = new BlockingRule(new ActualCopRule(triggerBuilderFactory));

    RuntimeRuleProvider provider = new RuntimeRuleProvider(rule);
    TestingItemRegistry registry = new TestingItemRegistry(heatProduced, energyConsumed, efficiency);
    NoRuleRegistry ruleRegistry = new NoRuleRegistry();
    ruleRegistry.addProvider(provider);
    NoRuleManager launcher = new NoRuleManager(ruleRegistry, thingRegistry, registry, new ReadyServiceImpl(), new DefaultThingActionsRegistry());

    new ItemMutation(energyConsumed).accept(new QuantityType<>(10, Units.KILOWATT_HOUR));

    StubEventBuilder.createItemStateChangedEvent(heatProduced,
      QuantityType.valueOf("50 kWh"), QuantityType.valueOf("9 kWh")
    ).build(launcher).fire();

    rule.getLatch().await();
    assertThat(efficiency.getState()).isEqualTo(new QuantityType<>(5, Units.ONE));
  }

}
