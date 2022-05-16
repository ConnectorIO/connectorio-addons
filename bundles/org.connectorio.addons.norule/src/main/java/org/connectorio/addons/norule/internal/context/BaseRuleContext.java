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
import java.util.function.BiConsumer;
import org.connectorio.addons.norule.Action;
import org.connectorio.addons.norule.ItemContext;
import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.RuleContext;
import org.connectorio.addons.norule.StateDispatcher;
import org.connectorio.addons.norule.ThingActionsRegistry;
import org.connectorio.addons.norule.Trigger;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRuleContext implements RuleContext {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final Rule rule;
  protected final ItemRegistry itemRegistry;
  protected final ThingActionsRegistry actionsRegistry;
  protected final StateDispatcher stateDispatcher;
  protected final Trigger trigger;


  public BaseRuleContext(Rule rule, ItemRegistry itemRegistry, ThingActionsRegistry actionsRegistry,
    StateDispatcher stateDispatcher, Trigger trigger) {
    this.rule = rule;
    this.itemRegistry = itemRegistry;
    this.actionsRegistry = actionsRegistry;
    this.stateDispatcher = stateDispatcher;
    this.trigger = trigger;
  }

  @Override
  public Trigger getTrigger() {
    return trigger;
  }

  @Override
  public ItemContext item(String itemName) {
    return Optional.ofNullable(itemRegistry.get(itemName))
      .<ItemContext>map(item -> new DefaultItemContext(stateDispatcher, item))
      .orElseGet(() -> new EmptyRuleContext(itemName));
  }

  @Override
  public <T> T getAction(String scope, ThingUID thing) {
    ThingActions actions = actionsRegistry.lookup(scope, thing).orElse(null);
    if (actions == null) {
      return null;
    }
    try {
      return (T) actions;
    } catch (ClassCastException e) {
      logger.warn("Could not cast action {} to desired type, returning null", e);
      return null;
    }
  }

  @Override
  public <T> Action<T> resolveAction(String scope, ThingUID thing) {
    return (Action<T>) actionsRegistry.lookupAction(scope, thing, getClassLoader()).orElse(null);
  }

  private ClassLoader getClassLoader() {
    return rule.getClass().getClassLoader();
  }
}
