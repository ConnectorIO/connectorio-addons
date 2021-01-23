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
package org.connectorio.addons.automation.calculation.internal.handler;

import java.util.Arrays;
import java.util.Collection;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ModuleHandlerFactory.class)
public class PersistenceServiceCalculationModuleHandlerFactory extends BaseModuleHandlerFactory {

  private static final Collection<String> TYPES = Arrays.asList(PersistenceServiceCalculationActionHandler.MODULE_TYPE_ID);

  private final EventPublisher eventPublisher;
  private final PersistenceServiceRegistry persistenceServiceRegistry;

  @Activate
  public PersistenceServiceCalculationModuleHandlerFactory(@Reference PersistenceServiceRegistry persistenceServiceRegistry, @Reference EventPublisher eventPublisher) {
    this.persistenceServiceRegistry = persistenceServiceRegistry;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public Collection<String> getTypes() {
    return TYPES;
  }

  @Override
  protected ModuleHandler internalCreate(Module module, String ruleUID) {
    return new PersistenceServiceCalculationActionHandler((Action) module, eventPublisher, persistenceServiceRegistry);
  }

}
