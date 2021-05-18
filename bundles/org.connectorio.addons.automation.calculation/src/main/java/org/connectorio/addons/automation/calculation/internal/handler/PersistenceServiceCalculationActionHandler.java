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

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import org.connectorio.addons.automation.calculation.CalculationConstants;
import org.connectorio.addons.automation.calculation.internal.config.PersistenceServiceCalculationConfig;
import org.connectorio.chrono.PeriodCalculator;
import org.connectorio.chrono.shared.FuturePeriodCalculator;
import org.connectorio.chrono.shared.PastPeriodCalculator;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.BaseActionModuleHandler;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.events.ItemEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceServiceCalculationActionHandler extends BaseActionModuleHandler {

  public static final String MODULE_TYPE_ID = "co7io.automation.persistenceServiceCalculation";

  private final Logger logger = LoggerFactory.getLogger(PersistenceServiceCalculationActionHandler.class);
  private final PersistenceServiceCalculationConfig config;
  private final Clock clock;
  private final EventPublisher eventPublisher;
  private final QueryablePersistenceService persistenceService;

  public PersistenceServiceCalculationActionHandler(Action module, Clock clock, EventPublisher eventPublisher, PersistenceServiceRegistry registry) {
    super(module);
    this.clock = clock;
    this.eventPublisher = eventPublisher;

    config = getConfigAs(PersistenceServiceCalculationConfig.class);

    if (config.input == null || config.input.trim().isEmpty()) {
      throw new IllegalArgumentException("Input item must be set");
    }

    if (config.output == null || config.output.trim().isEmpty()) {
      throw new IllegalArgumentException("Output item must be set");
    }

    if (config.serviceId == null || config.serviceId.trim().isEmpty()) {
      throw new IllegalArgumentException("Persistence service must be set");
    }

    PersistenceService service = registry.get(config.serviceId);
    if (!(service instanceof QueryablePersistenceService)) {
      throw new IllegalArgumentException("Persistence service not found or it isn't queryable");
    }

    persistenceService = (QueryablePersistenceService) service;
  }

  @Override
  public Map<String, Object> execute(Map<String, Object> context) {
    FilterCriteria from;
    FilterCriteria to;

    if (config.queryRange != null) {
      ZonedDateTime triggerTime = (ZonedDateTime) context.get("1." + CalculationConstants.TRIGGER_TIME);
      if (triggerTime == null) {
        triggerTime = ZonedDateTime.now(clock);
      }

      PeriodCalculator pastPeriodCalculator = new PastPeriodCalculator(Clock.fixed(triggerTime.toInstant(), clock.getZone()), config.offset, config.queryRange);
      if (config.offset != 0) {
        ZonedDateTime fromTime = pastPeriodCalculator.calculate();
        PeriodCalculator triggerPeriodCalculator = new FuturePeriodCalculator(Clock.fixed(fromTime.toInstant(), clock.getZone()), config.queryRange);
        from = createFilterCriteria(config.input, fromTime, true);
        to = createFilterCriteria(config.input, triggerPeriodCalculator.calculate(), false);
      } else {
        from = createFilterCriteria(config.input, pastPeriodCalculator.calculate(), true);
        to = createFilterCriteria(config.input, triggerTime, false);
      }
    } else {
      from = createFilterCriteria(config.input, (ZonedDateTime) context.get("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME), true);
      to = createFilterCriteria(config.input, (ZonedDateTime) context.get("1." + CalculationConstants.TRIGGER_TIME), false);
    }
    logger.debug("Persistence calculation with config {} and context {}. From {} to {}", config, context, from, to);

    if (from == null || to == null) {
      return null;
    }

    HistoricItem firstState = get(from);
    logger.debug("First state retrieved for time {}: {}", from, firstState != null ? firstState.getState() : null);
    if (firstState == null || firstState.getState() == null) {
      return Collections.singletonMap(CalculationConstants.RESULT, UnDefType.NULL);
    }

    HistoricItem secondState = get(to);
    logger.debug("Second state retrieved for time {}: {}", to, secondState != null ? secondState.getState() : null);
    if (secondState == null || secondState.getState() == null) {
      return Collections.singletonMap(CalculationConstants.RESULT, UnDefType.NULL);
    }

    State previous = firstState.getState();
    State current = secondState.getState();

    CalculationResult outputValue = calculate(previous, current);
    logger.debug("Result of calculation: {}", outputValue);
    if (outputValue == null) {
      return Collections.singletonMap(CalculationConstants.RESULT, UnDefType.NULL);
    }

    logger.debug("Updating state of item {} to {}", config.output, outputValue.presentUsage);
    final ItemEvent itemCommandEvent = ItemEventFactory.createStateEvent(config.output, outputValue.presentUsage);
    eventPublisher.post(itemCommandEvent);

    return Collections.singletonMap(CalculationConstants.RESULT, outputValue.presentUsage);
  }

  public CalculationResult calculate(State previous, State current) {
    if (current instanceof QuantityType) {
      QuantityType to = (QuantityType<?>) current;
      if (previous instanceof QuantityType) {
        QuantityType from = (QuantityType<?>) previous;
        return new CalculationResult(current, to.subtract(from));
      }
    }

    if (current instanceof DecimalType) {
      DecimalType to = (DecimalType) current;
      if (previous instanceof DecimalType) {
        DecimalType from = (DecimalType) previous;
        return new CalculationResult(current, new DecimalType(to.toBigDecimal().subtract(from.toBigDecimal())));
      }
    }

    return new CalculationResult(current, UnDefType.NULL);
  }

  public HistoricItem get(FilterCriteria filter) {
    Iterable<HistoricItem> result = persistenceService.query(filter);

    if (result.iterator().hasNext()) {
      return result.iterator().next();
    }

    return null;
  }

  private FilterCriteria createFilterCriteria(String item, ZonedDateTime timestamp, boolean start) {
    if (timestamp == null) {
      return null;
    }
    FilterCriteria filter = new FilterCriteria() {
      @Override
      public String toString() {
        return "FilterCriteria [begin=" + getBeginDate() + ", end=" + getEndDate() + ", operator=" + getOperator() + ", ordering=" + getOrdering() + "]";
      }
    };
    if (start) {
      filter.setBeginDate(timestamp).setOrdering(Ordering.ASCENDING).setOperator(Operator.GTE);
    } else {
      filter.setEndDate(timestamp).setOrdering(Ordering.DESCENDING).setOperator(Operator.LTE);
    }
    filter.setItemName(item);
    filter.setPageSize(1);
    return filter;
  }

  PersistenceServiceCalculationConfig getConfiguration() {
    return config;
  }

}
