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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.connectorio.addons.automation.calculation.CalculationConstants;
import org.connectorio.chrono.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.openhab.core.automation.internal.ActionImpl;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;

@ExtendWith(MockitoExtension.class)
class PastPersistenceServiceCalculationActionHandlerTest extends BasePersistenceServiceCalculationTest {

  @BeforeEach
  void setup() {
    Map<String, Object> config = new HashMap<>();
    config.put("input", ITEM_ENERGY_READING);
    config.put("output", ITEM_ENERGY_USE);
    config.put("serviceId", PERSISTENCE_SERVICE_ID);
    config.put("queryRange", Period.MONTH.name());

    when(persistenceServiceRegistry.get(PERSISTENCE_SERVICE_ID)).thenReturn(persistenceService);

    Configuration cfg = new Configuration(config);
    this.handler = new PersistenceServiceCalculationActionHandler(
      new ActionImpl("test", "test", cfg, null, null, null),
      Clock.systemUTC(), eventPublisher, persistenceServiceRegistry
    );
  }

  @Test
  void testCalculationLogic() throws Exception {
    ZonedDateTime beginningOfMonth = createInstant(2020, 10, 1, 0, 0, 0);
    ZonedDateTime firstTriggerTime = createInstant(2020, 10, 12, 13, 15, 0);
    ZonedDateTime secondTriggerTime = createInstant(2020, 10, 12, 13, 16, 0);
    ZonedDateTime lastTrigger = createInstant(2020, 10, 12, 13, 17, 0);

    Map<ZonedDateTime, HistoricItem> readings = new HashMap<>();
    createMockItem(100036, beginningOfMonth, readings);
    createMockItem(100136, secondTriggerTime, readings);
    createMockItem(100137.01, lastTrigger, readings);

    when(persistenceService.query(any())).thenAnswer(new ReadingAnswer(readings));

    Map<String, Object> context = new HashMap<>();
    context.put("1." + CalculationConstants.TRIGGER_TIME, secondTriggerTime);
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, firstTriggerTime);
    Map<String, Object> result = handler.execute(context);
    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(100.0, Units.KILOWATT_HOUR));

    context.put("1." + CalculationConstants.TRIGGER_TIME, lastTrigger);
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, secondTriggerTime);
    result = handler.execute(context);

    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(101.01, Units.KILOWATT_HOUR));
  }


  @Test
  void testEdgeCalculationLogic() throws Exception {
    ZonedDateTime beginningOfMonth = createInstant(2020, 10, 1, 0, 0, 0);
    ZonedDateTime firstTriggerTime = createInstant(2020, 10, 30, 0, 0, 0);
    ZonedDateTime secondTriggerTime = createInstant(2020, 10, 31, 0, 0, 0);
    ZonedDateTime lastTrigger = createInstant(2020, 11, 30, 0, 0, 0);

    Map<ZonedDateTime, HistoricItem> readings = new HashMap<>();
    createMockItem(100036, beginningOfMonth, readings);
    createMockItem(100136, secondTriggerTime, readings);
    createMockItem(100136, beginningOfMonth.plusMonths(1), readings);
    createMockItem(100237.01, lastTrigger, readings);

    when(persistenceService.query(any())).thenAnswer(new ReadingAnswer(readings));

    Map<String, Object> context = new HashMap<>();
    context.put("1." + CalculationConstants.TRIGGER_TIME, secondTriggerTime);
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, firstTriggerTime);
    Map<String, Object> result = handler.execute(context);
    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(100.0, Units.KILOWATT_HOUR));

    context.put("1." + CalculationConstants.TRIGGER_TIME, lastTrigger);
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, secondTriggerTime);
    result = handler.execute(context);

    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(101.01, Units.KILOWATT_HOUR));
  }

  @Test
  void testMonthlyExecution() throws Exception {
    ZonedDateTime beginningOfSeptember = createInstant(2020, 9, 1, 0, 0, 0);
    ZonedDateTime beginningOfOctober = createInstant(2020, 10, 1, 0, 0, 0);

    ZonedDateTime triggerTime = createInstant(2020, 10, 1, 13, 10, 51);

    Map<ZonedDateTime, HistoricItem> readings = new HashMap<>();
    createMockItem(100036, beginningOfSeptember, readings);
    createMockItem(100137.01, beginningOfOctober, readings);

    when(persistenceService.query(any())).thenAnswer(new ReadingAnswer(readings));

    Map<String, Object> context = new HashMap<>();
    context.put("1." + CalculationConstants.TRIGGER_TIME, triggerTime);

    handler.getConfiguration().offset = 1;
    Map<String, Object> result = handler.execute(context);

    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(101.01, Units.KILOWATT_HOUR));
  }
}