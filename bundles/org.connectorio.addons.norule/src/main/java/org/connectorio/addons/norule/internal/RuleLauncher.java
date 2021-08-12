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
package org.connectorio.addons.norule.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.RuleContext;
import org.connectorio.addons.norule.Trigger;
import org.connectorio.addons.norule.internal.context.ItemStateContext;
import org.connectorio.addons.norule.internal.trigger.GroupStateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.StateChangeTrigger;
import org.connectorio.addons.norule.internal.trigger.StateUpdateTrigger;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.GroupItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleLauncher implements EventSubscriber {

  private final Logger logger = LoggerFactory.getLogger(RuleLauncher.class);
  private final List<Rule> rules = new CopyOnWriteArrayList<>();
  private final ItemRegistry itemRegistry;

  @Activate
  public RuleLauncher(ItemRegistry itemRegistry) {
    this.itemRegistry = itemRegistry;
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE)
  public void addRule(Rule rule) {
    this.rules.add(rule);
  }

  public void removeRule(Rule rule) {
    this.rules.remove(rule);
  }

  @Override
  public Set<String> getSubscribedEventTypes() {
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      GroupItemStateChangedEvent.TYPE,
      ItemStateEvent.TYPE,
      ItemStateChangedEvent.TYPE
    )));
  }

  @Override
  public EventFilter getEventFilter() {
    return null;
  }

  @Override
  public void receive(Event event) {
    if (event instanceof GroupItemStateChangedEvent) {
      GroupItemStateChangedEvent stateChangeEvent = (GroupItemStateChangedEvent) event;
      fire(new ItemStateContext(itemRegistry, stateChangeEvent.getItemName(), stateChangeEvent.getItemState()), (trigger) -> {
        return trigger instanceof GroupStateChangeTrigger && stateChangeEvent.getItemName().equals(((GroupStateChangeTrigger) trigger).getGroupName());
      });
    } else if (event instanceof ItemStateChangedEvent) {
      ItemStateChangedEvent stateChangeEvent = (ItemStateChangedEvent) event;
      fire(new ItemStateContext(itemRegistry, stateChangeEvent.getItemName(), stateChangeEvent.getItemState()), (trigger) -> {
        return trigger instanceof StateChangeTrigger && stateChangeEvent.getItemName().equals(((StateChangeTrigger) trigger).getItemName());
      });
    } else if (event instanceof ItemStateEvent) {
      ItemStateEvent stateEvent = (ItemStateEvent) event;
      fire(new ItemStateContext(itemRegistry, stateEvent.getItemName(), stateEvent.getItemState()), (trigger -> {
        return trigger instanceof StateUpdateTrigger && stateEvent.getItemName().equals(((StateUpdateTrigger) trigger).getItemName());
      }));
    } else {
      logger.debug("Unsupported event received {}", event);
    }
  }

  private final void fire(RuleContext context, Predicate<Trigger> predicate) {
    for (Rule rule : rules) {
      boolean fire = false;
      for (Trigger trigger : rule.getTriggers()) {
        if (predicate.test(trigger)) {
          fire = true;
        }
      }
      if (fire) {
        rule.handle(context);
      }
    }
  }

}
