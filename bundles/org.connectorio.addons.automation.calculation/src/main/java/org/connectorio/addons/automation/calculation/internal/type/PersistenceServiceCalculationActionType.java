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
package org.connectorio.addons.automation.calculation.internal.type;

import static org.connectorio.addons.automation.calculation.CalculationConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.connectorio.addons.automation.calculation.internal.handler.PersistenceServiceCalculationActionHandler;
import org.connectorio.addons.automation.calculation.internal.handler.VolatileCalculationActionHandler;
import org.connectorio.chrono.Period;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.persistence.PersistenceManager;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;

public class PersistenceServiceCalculationActionType extends VolatileCalculationActionType {

  public PersistenceServiceCalculationActionType(PersistenceServiceRegistry persistenceServiceRegistry) {
    this(createConfigDescription(persistenceServiceRegistry), createInputs());
  }

  public PersistenceServiceCalculationActionType(List<ConfigDescriptionParameter> configDescriptions, List<Input> inputs) {
    super(PersistenceServiceCalculationActionHandler.MODULE_TYPE_ID, configDescriptions, "Persistence service calculation",
      "This calculation rely on queries executed in persistence service in order to retrieve readings in given period of time", null,
      Visibility.VISIBLE, inputs, null);
  }

  private static List<ConfigDescriptionParameter> createConfigDescription(PersistenceServiceRegistry persistenceServiceRegistry) {
    final ConfigDescriptionParameter inputItem = ConfigDescriptionParameterBuilder.create("input", Type.TEXT)
      .withRequired(true).withMultiple(false).withContext("item").withLabel("Input item")
      .withDescription("Item which collect readings (increasing or decreasing value)")
      .build();

    final ConfigDescriptionParameter outputItem = ConfigDescriptionParameterBuilder.create("output", Type.TEXT)
      .withRequired(true).withMultiple(false).withContext("item").withLabel("Output item")
      .withDescription("Item which receive calculated values")
      .build();

    List<ParameterOption> services = persistenceServiceRegistry.getAll().stream()
      .filter(service -> service instanceof QueryablePersistenceService)
      .map(service -> new ParameterOption(service.getId(), service.getLabel(null)))
      .collect(Collectors.toList());

    final ConfigDescriptionParameter serviceId = ConfigDescriptionParameterBuilder.create("serviceId", Type.TEXT)
      .withRequired(true).withMultiple(false).withLabel("Persistence service")
      .withDefault(persistenceServiceRegistry.getDefaultId())
      .withDescription("Service which should be queried for historical readings")
      .withOptions(services)
      .build();

    List<ParameterOption> queryRange = Arrays.stream(Period.values())
      .map(period -> new ParameterOption(period.name(), period.getLabel()))
      .collect(Collectors.toList());
    final ConfigDescriptionParameter queryPeriod = ConfigDescriptionParameterBuilder.create("queryRange", Type.TEXT)
      .withRequired(false).withMultiple(false).withLabel("Query range")
      .withDescription("Period for query - if different than trigger range")
      .withOptions(queryRange)
      .build();

    final ConfigDescriptionParameter offset = ConfigDescriptionParameterBuilder.create("offset", Type.INTEGER)
      .withMinimum(BigDecimal.ZERO)
      .withRequired(false).withMultiple(false).withLabel("Query offset")
      .withDescription("An number of years/days to be shifted when running calculation, for previous day/year combined with period trigger set value to 1")
      .build();

    final ConfigDescriptionParameter triggerTime = ConfigDescriptionParameterBuilder.create(TRIGGER_TIME, Type.TEXT)
      .withRequired(false).withMultiple(false).withLabel("Trigger time")
      .withDescription("Period for query - if different than trigger range")
      .build();
    final ConfigDescriptionParameter previousTriggerTime = ConfigDescriptionParameterBuilder.create(PREVIOUS_TRIGGER_TIME, Type.TEXT)
      .withRequired(false).withMultiple(false).withLabel("Trigger time")
      .withDescription("Period for query - if different than trigger range")
      .build();

    return Arrays.asList(inputItem, outputItem, serviceId, queryPeriod, offset, triggerTime, previousTriggerTime);
  }

  private static List<Input> createInputs() {
    return Arrays.asList(
      new Input(TRIGGER_TIME, Instant.class.getName()),
      new Input(PREVIOUS_TRIGGER_TIME, Instant.class.getName())
    );
  }
}
