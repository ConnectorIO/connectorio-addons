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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.connectorio.addons.automation.calculation.internal.handler.VolatileCalculationActionHandler;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.automation.type.Output;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;

public class VolatileCalculationActionType extends ActionType {

  public static final VolatileCalculationActionType INSTANCE = new VolatileCalculationActionType();

  public VolatileCalculationActionType() {
    this(createConfigDescription(), null);
  }

  public VolatileCalculationActionType(List<ConfigDescriptionParameter> configDescriptions, List<Input> inputs) {
    this(VolatileCalculationActionHandler.MODULE_TYPE_ID, configDescriptions, "Volatile calculation", null, null,
      Visibility.VISIBLE, inputs, null);
  }

  protected VolatileCalculationActionType(String UID, List<ConfigDescriptionParameter> configDescriptions,
    String label, String description, Set<String> tags, Visibility visibility, List<Input> inputs, List<Output> outputs) {
    super(UID, configDescriptions, label, description, tags, visibility, inputs, outputs);
  }

  private static List<ConfigDescriptionParameter> createConfigDescription() {
    final ConfigDescriptionParameter inputItem = ConfigDescriptionParameterBuilder.create("input", Type.TEXT)
      .withRequired(true).withMultiple(false).withContext("item").withLabel("Input item")
      .withDescription("Item which collect readings (increasing or decreasing value)")
      .build();

    final ConfigDescriptionParameter outputItem = ConfigDescriptionParameterBuilder.create("output", Type.TEXT)
      .withRequired(true).withMultiple(false).withContext("item").withLabel("Output item")
      .withDescription("Item which receive calculated values")
      .build();

    return Arrays.asList(inputItem, outputItem);
  }

}
