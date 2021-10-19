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
package org.connectorio.addons.norule.internal.action;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.norule.Action;
import org.connectorio.addons.norule.ThingActionsRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.UID;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component
public class DefaultThingActionsRegistry implements ThingActionsRegistry {

  private final Map<ActionKey, ThingActions> actions = new ConcurrentHashMap<>();

  @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
  synchronized void addThingActions(ThingActions thingActions) {
    actions.put(new ActionKey(thingActions), thingActions);
  }

  protected void removeThingActions(ThingActions thingActions) {
    actions.remove(new ActionKey(thingActions));
  }

  @Override
  public Optional<ThingActions> lookup(String scope, ThingUID thing) {
    return Optional.ofNullable(actions.get(new ActionKey(scope, thing.getAsString())));
  }

  @Override
  public <T> Optional<Action<T>> lookupAction(String scope, ThingUID thing, ClassLoader classLoader) {
    return lookup(scope, thing)
      .map(action -> new WrappedActions<>(action, classLoader));
  }

  static class ActionKey {
    final String scope;
    final String id;

    ActionKey(ThingActions actions) {
      this.scope = resolveScope(actions);
      this.id = resolveId(actions);
    }

    ActionKey(String scope, String id) {
      this.scope = scope;
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ActionKey)) {
        return false;
      }
      ActionKey actionKey = (ActionKey) o;
      return Objects.equals(scope, actionKey.scope) && Objects.equals(id, actionKey.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(scope, id);
    }

    private static String resolveId(ThingActions thingActions) {
      return Optional.of(thingActions)
        .map(ThingHandlerService::getThingHandler)
        .map(ThingHandler::getThing)
        .map(Thing::getUID)
        .map(UID::toString)
        .orElse("");
    }

    private static String resolveScope(ThingActions thingActions) {
      return Optional.of(thingActions)
        .map(actions -> actions.getClass().getAnnotation(ThingActionsScope.class))
        .map(ThingActionsScope::name)
        .orElse("");
    }
  }

}
