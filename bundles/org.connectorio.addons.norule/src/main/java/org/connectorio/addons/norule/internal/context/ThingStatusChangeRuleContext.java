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
import org.connectorio.addons.norule.context.ThingStatusChangeContext;
import org.connectorio.addons.norule.context.ThingStatusContext;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;

public class ThingStatusChangeRuleContext extends ThingStatusRuleContext implements ThingStatusChangeContext {

  private final ThingStatusInfo previousStatus;

  public ThingStatusChangeRuleContext(Rule rule, ItemRegistry itemRegistry, ThingActionsRegistry actionsRegistry, Trigger trigger, Thing thing, ThingStatusInfo currentStatus, ThingStatusInfo previousStatus) {
    super(rule, itemRegistry, actionsRegistry, trigger, thing, currentStatus);
    this.previousStatus = previousStatus;
  }

  @Override
  public ThingStatusInfo previousStatus() {
    return previousStatus;
  }

}
