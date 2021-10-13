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
import javax.measure.quantity.Energy;
import org.openhab.core.library.types.QuantityType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simplistic rule to calculate efficiency of heat production.
 */
@Component
public class ActualCopRule implements Rule {

  private final Set<Trigger> triggers;

  @Activate
  public ActualCopRule(@Reference TriggerBuilderFactory triggerFactory) {
    this.triggers = triggerFactory.createBuilder()
      .itemStateChange("HeatProduced")
      .itemStateChange("EnergyConsumed")
      .build();
  }

  @Override
  public Set<Trigger> getTriggers() {
    return triggers;
  }

  @Override
  public void handle(RuleContext context) {
    QuantityType<Energy> heat = context.item("HeatProduced").state(QuantityType.class)
      .map(q -> (QuantityType<Energy>) q)
      .orElseThrow(SkipExecutionException::new);

    QuantityType<Energy> energy = context.item("EnergyConsumed").state(QuantityType.class)
        .map(q -> (QuantityType<Energy>) q)
      .orElseThrow(SkipExecutionException::new);

    context.item("Efficiency").state(
      heat.divide(energy)
    );
  }

  @Override
  public RuleUID getUID() {
    return new RuleUID("norule", "cop");
  }
}
