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
package org.connectorio.addons.norule.internal.condition;

import java.util.LinkedHashSet;
import java.util.Set;
import org.connectorio.addons.norule.Condition;
import org.connectorio.addons.norule.ConditionBuilder;
import org.openhab.core.addon.AddonInfoRegistry;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.StartLevelService;

public class DefaultConditionBuilder implements ConditionBuilder {

  private final ReadyService readyService;
  private final AddonInfoRegistry bindingInfoRegistry;
  private final Set<Condition> conditions = new LinkedHashSet<>();

  public DefaultConditionBuilder(ReadyService readyService, AddonInfoRegistry bindingInfoRegistry) {
    this.readyService = readyService;
    this.bindingInfoRegistry = bindingInfoRegistry;
  }

  @Override
  public ConditionBuilder systemIsStarted() {
    conditions.add(new ReadyMarkerCondition(readyService, new ReadyMarker(
      StartLevelService.STARTLEVEL_MARKER_TYPE, "" + StartLevelService.STARTLEVEL_COMPLETE
    )));
    return this;
  }

  @Override
  public ConditionBuilder startLevelReached(int level) {
    conditions.add(new ReadyMarkerCondition(readyService, new ReadyMarker(
      StartLevelService.STARTLEVEL_MARKER_TYPE, "" + level
    )));
    return this;
  }

  @Override
  public ConditionBuilder readyMarkerReached(ReadyMarker readyMarker) {
    conditions.add(new ReadyMarkerCondition(readyService, readyMarker));
    return this;
  }

  @Override
  public ConditionBuilder hasBinding(String id) {
    conditions.add(new HasBindingCondition(bindingInfoRegistry, id));
    return this;
  }

  @Override
  public Set<Condition> build() {
    return conditions;
  }

}
