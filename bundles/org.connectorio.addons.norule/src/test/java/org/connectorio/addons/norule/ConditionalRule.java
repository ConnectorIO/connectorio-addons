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

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.measure.quantity.Energy;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simplistic rule to calculate efficiency of heat production.
 */
@Component
public class ConditionalRule implements Rule {

  public static final ThingUID TEST_THING = new ThingUID("foo", "bar");

  private final Set<Trigger> triggers;
  private final Set<Condition> conditions;
  private final AtomicBoolean executed = new AtomicBoolean();

  @Activate
  public ConditionalRule(@Reference TriggerBuilderFactory triggerFactory, @Reference ConditionBuilderFactory conditionBuilderFactory) {
    this.triggers = triggerFactory.createBuilder()
      .thingStatusChange(TEST_THING)
      .build();
    this.conditions = conditionBuilderFactory.createBuilder()
      .systemIsStarted()
      .build();
  }

  @Override
  public Set<Trigger> getTriggers() {
    return triggers;
  }

  @Override
  public Set<Condition> getConditions() {
    return conditions;
  }

  @Override
  public void handle(RuleContext context) {
    executed.set(true);
  }

  public boolean isExecuted() {
    return executed.get();
  }

  @Override
  public RuleUID getUID() {
    return new RuleUID("norule", "status");
  }
}
