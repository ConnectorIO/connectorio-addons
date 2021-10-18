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

import java.util.Optional;
import org.connectorio.addons.norule.ItemContext;
import org.connectorio.addons.norule.RuleContext;
import org.connectorio.addons.norule.Trigger;
import org.connectorio.addons.norule.internal.ThingsActionsRegistry;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingActions;

public abstract class BaseRuleContext implements RuleContext {

  private final ItemRegistry itemRegistry;
  private final ThingsActionsRegistry actionsRegistry;
  private final Trigger trigger;

  public BaseRuleContext(ItemRegistry itemRegistry, ThingsActionsRegistry actionsRegistry, Trigger trigger) {
    this.itemRegistry = itemRegistry;
    this.actionsRegistry = actionsRegistry;
    this.trigger = trigger;
  }

  @Override
  public Trigger getTrigger() {
    return trigger;
  }

  @Override
  public ItemContext item(String itemName) {
    return Optional.ofNullable(itemRegistry.get(itemName))
      .<ItemContext>map(DefaultItemContext::new)
      .orElseGet(EmptyItemContext::new);
  }

  @Override
  public <T extends ThingActions> T getAction(ThingUID thing) {
    ThingActions actions = actionsRegistry.lookup(thing).orElse(null);
    if (actions == null) {
      return null;
    }
    return (T) actions;
  }
}
