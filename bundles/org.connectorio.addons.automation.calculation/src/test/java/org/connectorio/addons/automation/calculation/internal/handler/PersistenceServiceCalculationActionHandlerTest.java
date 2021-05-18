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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.internal.ActionImpl;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;

@ExtendWith(MockitoExtension.class)
class PersistenceServiceCalculationActionHandlerTest extends BasePersistenceServiceCalculationTest {

  @BeforeEach
  void setup() {
    Map<String, Object> config = new HashMap<>();
    config.put("input", ITEM_ENERGY_READING);
    config.put("output", ITEM_ENERGY_USE);
    config.put("serviceId", PERSISTENCE_SERVICE_ID);

    when(persistenceServiceRegistry.get(PERSISTENCE_SERVICE_ID)).thenReturn(persistenceService);

    Configuration cfg = new Configuration(config);
    this.handler = new PersistenceServiceCalculationActionHandler(
      new ActionImpl("test", "test", cfg, null, null, null),
      Clock.systemUTC(), eventPublisher, persistenceServiceRegistry
    );
  }

  //@Test @Ignore
  void testCalculationLogic() throws Exception {
    ZonedDateTime previousTrigger = createInstant(2020, 12, 12, 1, 0, 0);
    ZonedDateTime currentTrigger = createInstant(2020, 12, 12, 2, 0, 0);
    ZonedDateTime nextTrigger = createInstant(2020, 12, 12, 3, 0, 0);

    Map<ZonedDateTime, HistoricItem> readings = new HashMap<>();
    HistoricItem firstItem = createMockItem(100036, previousTrigger, readings);
    HistoricItem secondItem = createMockItem(100036, currentTrigger, readings);
    HistoricItem nextItem = createMockItem(100037.01, nextTrigger, readings);

    when(persistenceService.query(any())).thenAnswer(inv -> {
      FilterCriteria argument = inv.getArgument(0, FilterCriteria.class);
      if (argument == null) {
        return Collections.emptyList();
      }

      ZonedDateTime dateTime = Optional.ofNullable(argument.getBeginDate()).orElse(argument.getEndDate());
      return Collections.singletonList(readings.get(dateTime));
    });

    Map<String, Object> context = new HashMap<>();
    context.put("1." + CalculationConstants.TRIGGER_TIME, currentTrigger);
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, previousTrigger);
    Map<String, Object> result = handler.execute(context);
    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(0.0, Units.KILOWATT_HOUR));

    context.put("1." + CalculationConstants.TRIGGER_TIME, nextTrigger);
    context.put("1." + CalculationConstants.PREVIOUS_TRIGGER_TIME, currentTrigger);
    result = handler.execute(context);

    assertThat(result).isNotNull()
      .containsEntry(CalculationConstants.RESULT, new QuantityType<>(1.01, Units.KILOWATT_HOUR));
  }

}