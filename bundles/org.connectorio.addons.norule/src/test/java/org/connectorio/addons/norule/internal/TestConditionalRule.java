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
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.connectorio.addons.norule.BlockingRule;
import org.connectorio.addons.norule.ConditionalRule;
import org.connectorio.addons.norule.internal.action.DefaultThingActionsRegistry;
import org.connectorio.addons.norule.internal.condition.DefaultConditionBuilderFactory;
import org.connectorio.addons.test.StubEventBuilder;
import org.connectorio.addons.test.TestingItemRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.addon.AddonInfoRegistry;
import org.openhab.core.internal.service.ReadyServiceImpl;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.StartLevelService;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

@ExtendWith(MockitoExtension.class)
public class TestConditionalRule {

  @Mock
  private ThingRegistry thingRegistry;

  @Mock
  private ReadyService readyService;

  @Mock
  private AddonInfoRegistry bindingInfoRegistry;

  @Test
  void checkBasicRule() throws InterruptedException {
    DefaultTriggerBuilderFactory triggerBuilderFactory = new DefaultTriggerBuilderFactory();
    DefaultConditionBuilderFactory conditionBuilderFactory = new DefaultConditionBuilderFactory(readyService, bindingInfoRegistry);

    ConditionalRule conditional = new ConditionalRule(triggerBuilderFactory, conditionBuilderFactory);
    BlockingRule rule = new BlockingRule(conditional);

    RuntimeRuleProvider provider = new RuntimeRuleProvider(rule);
    TestingItemRegistry registry = new TestingItemRegistry();
    NoRuleRegistry ruleRegistry = new NoRuleRegistry();
    ruleRegistry.addProvider(provider);
    NoRuleManager launcher = new NoRuleManager(ruleRegistry, thingRegistry, registry, new ReadyServiceImpl(), new DefaultThingActionsRegistry());

    StubEventBuilder.createThingStatusInfoChangedEvent(ConditionalRule.TEST_THING,
      ThingStatusInfoBuilder.create(ThingStatus.OFFLINE).build(), ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build()
    ).build(launcher).fire();
    // make sure event does not trigger rule

    // if we succeed to unblock before 500 ms then rule got executed despite of condition
    // next assert should report an error!
    try {
      rule.getLatch().await(500, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      // we expect failure here
    }
    assertThat(conditional.isExecuted()).isEqualTo(false);

    // stub system readiness
    when(readyService.isReady(new ReadyMarker(StartLevelService.STARTLEVEL_MARKER_TYPE, "" + StartLevelService.STARTLEVEL_COMPLETE)))
      .thenReturn(true);

    // fire again status change
    StubEventBuilder.createThingStatusInfoChangedEvent(ConditionalRule.TEST_THING,
      ThingStatusInfoBuilder.create(ThingStatus.OFFLINE).build(), ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build()
    ).build(launcher).fire();

    rule.getLatch().await();
    assertThat(conditional.isExecuted()).isEqualTo(true);
  }

}
