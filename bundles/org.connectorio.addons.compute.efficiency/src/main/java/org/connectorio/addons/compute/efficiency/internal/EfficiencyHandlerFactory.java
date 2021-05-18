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
package org.connectorio.addons.compute.efficiency.internal;

import org.connectorio.addons.binding.handler.factory.BaseThingHandlerFactory;
import org.connectorio.addons.compute.efficiency.internal.memo.StateCollector;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EfficiencyHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
@Component(configurationPid = "binding.efficiency", service = ThingHandlerFactory.class)
public class EfficiencyHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

  private final ItemRegistry itemRegistry;
  private final StateCollector<ItemStateChangedEvent> collector;

  @Activate
  public EfficiencyHandlerFactory(@Reference ItemRegistry itemRegistry, @Reference StateCollector<ItemStateChangedEvent> collector) {
    super(EfficiencyBindingConstants.THING_TYPE_VENTILATION_HEAT_EXCHANGER);
    this.itemRegistry = itemRegistry;
    this.collector = collector;
  }

  @Override
  protected ThingHandler createHandler(Thing thing) {
    ThingTypeUID thingTypeUID = thing.getThingTypeUID();

    if (EfficiencyBindingConstants.THING_TYPE_VENTILATION_HEAT_EXCHANGER.equals(thingTypeUID)) {
      return new EfficiencyHandler(thing, itemRegistry, collector);
    }

    return null;
  }
}
