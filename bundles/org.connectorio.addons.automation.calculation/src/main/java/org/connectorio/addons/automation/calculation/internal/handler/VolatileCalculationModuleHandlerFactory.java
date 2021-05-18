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
package org.connectorio.addons.automation.calculation.internal.handler;

import java.util.Arrays;
import java.util.Collection;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.ItemRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ModuleHandlerFactory.class)
public class VolatileCalculationModuleHandlerFactory extends BaseModuleHandlerFactory {

  private static final Collection<String> TYPES = Arrays.asList(VolatileCalculationActionHandler.MODULE_TYPE_ID);
  private final ItemRegistry itemRegistry;
  private final EventPublisher eventPublisher;

  @Activate
  public VolatileCalculationModuleHandlerFactory(@Reference ItemRegistry itemRegistry, @Reference EventPublisher eventPublisher) {
    this.itemRegistry = itemRegistry;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public Collection<String> getTypes() {
    return TYPES;
  }

  @Override
  protected ModuleHandler internalCreate(Module module, String ruleUID) {
    return new VolatileCalculationActionHandler((Action) module, eventPublisher, itemRegistry);
  }

}
