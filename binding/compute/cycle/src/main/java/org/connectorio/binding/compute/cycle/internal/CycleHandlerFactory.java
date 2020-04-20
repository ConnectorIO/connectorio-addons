/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.binding.compute.cycle.internal;

import static org.connectorio.binding.compute.cycle.internal.CycleBindingConstants.THING_TYPE_CYCLE_COUNTER;

import org.connectorio.binding.base.handler.factory.BaseThingHandlerFactory;
import org.connectorio.binding.compute.cycle.internal.memo.StateCollector;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link CycleHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.cycle", service = ThingHandlerFactory.class)
public class CycleHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final ItemRegistry itemRegistry;
  private final StateCollector<ItemStateChangedEvent> collector;

  @Activate
  public CycleHandlerFactory(@Reference ItemRegistry itemRegistry, @Reference StateCollector<ItemStateChangedEvent> collector) {
    super(THING_TYPE_CYCLE_COUNTER);
    this.itemRegistry = itemRegistry;
    this.collector = collector;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (THING_TYPE_CYCLE_COUNTER.equals(thingTypeUID)) {
      return new CycleCounterHandler(thing, itemRegistry, collector);
    }

    return null;
  }
}
