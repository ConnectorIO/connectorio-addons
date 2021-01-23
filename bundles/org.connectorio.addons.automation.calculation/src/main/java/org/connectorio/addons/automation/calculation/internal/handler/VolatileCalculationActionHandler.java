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
package org.connectorio.addons.automation.calculation.internal.handler;

import java.util.Collections;
import java.util.Map;
import org.connectorio.addons.automation.calculation.CalculationConstants;
import org.connectorio.addons.automation.calculation.internal.config.VolatileCalculationConfig;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.BaseActionModuleHandler;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VolatileCalculationActionHandler extends BaseActionModuleHandler {

  public static final String MODULE_TYPE_ID = "co7io.automation.volatileCalculation";

  private final Logger logger = LoggerFactory.getLogger(VolatileCalculationActionHandler.class);
  private final VolatileCalculationConfig config;
  private final ItemRegistry itemRegistry;
  private final EventPublisher eventPublisher;

  private State previous;

  public VolatileCalculationActionHandler(Action module, EventPublisher eventPublisher, ItemRegistry itemRegistry) {
    super(module);
    this.itemRegistry = itemRegistry;
    this.eventPublisher = eventPublisher;

    config = getConfigAs(VolatileCalculationConfig.class);

    if (config.input == null || config.input.trim().isEmpty()) {
      throw new IllegalArgumentException("Input item must be set");
    }

    if (config.output == null || config.output.trim().isEmpty()) {
      throw new IllegalArgumentException("Output item must be set");
    }
  }

  @Override
  public Map<String, Object> execute(Map<String, Object> context) {
    try {
      Item item = itemRegistry.getItem(config.input);
      State state = item.getState();
      CalculationResult outputValue = calculate(state);
      if (outputValue == null) {
        return Collections.singletonMap(CalculationConstants.RESULT, UnDefType.NULL);
      }
      previous = outputValue.previousReading; // retain previous value

      final ItemEvent itemCommandEvent = ItemEventFactory.createStateEvent(config.output, outputValue.presentUsage);
      eventPublisher.post(itemCommandEvent);

      return Collections.singletonMap(CalculationConstants.RESULT, outputValue.presentUsage);
    } catch (ItemNotFoundException e) {
      logger.warn("Input item {} not found", config.input);
      return Collections.singletonMap(CalculationConstants.RESULT, UnDefType.NULL);
    }
  }

  public CalculationResult calculate(State state) {
    if (state instanceof QuantityType) {
      QuantityType to = (QuantityType<?>) state;
      if (previous instanceof QuantityType) {
        QuantityType from = (QuantityType<?>) previous;
        return new CalculationResult(state, to.subtract(from));
      }
    }

    if (state instanceof DecimalType) {
      DecimalType to = (DecimalType) state;
      if (previous instanceof DecimalType) {
        DecimalType from = (DecimalType) previous;
        return new CalculationResult(state, new DecimalType(to.toBigDecimal().subtract(from.toBigDecimal())));
      }
    }

    return new CalculationResult(state, UnDefType.NULL);
  }

}
