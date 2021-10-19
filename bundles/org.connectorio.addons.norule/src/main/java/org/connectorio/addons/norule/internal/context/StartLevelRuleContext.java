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
package org.connectorio.addons.norule.internal.context;

import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.ThingActionsRegistry;
import org.connectorio.addons.norule.Trigger;
import org.connectorio.addons.norule.context.StartLevelContext;
import org.openhab.core.items.ItemRegistry;

public class StartLevelRuleContext extends BaseRuleContext implements StartLevelContext {

  private final int oldStartLevel;
  private final int startLevel;

  public StartLevelRuleContext(Rule rule, ItemRegistry itemRegistry, ThingActionsRegistry actionsRegistry, Trigger trigger, int oldStartLevel, int startLevel) {
    super(rule, itemRegistry, actionsRegistry, trigger);
    this.oldStartLevel = oldStartLevel;
    this.startLevel = startLevel;
  }

  public int previousStartLevel() {
    return oldStartLevel;
  }

  public int currentStartLevel() {
    return startLevel;
  }

}
