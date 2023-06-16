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
package org.connectorio.addons.persistence.manager.internal;

import org.openhab.core.common.SafeCaller;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.internal.PersistenceManagerImpl;
import org.openhab.core.persistence.registry.PersistenceServiceConfigurationRegistry;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.Scheduler;
import org.openhab.core.service.ReadyService;

public class AccessiblePersistenceManager extends PersistenceManagerImpl {

  public AccessiblePersistenceManager(CronScheduler cronScheduler, Scheduler scheduler, ItemRegistry itemRegistry, SafeCaller safeCaller, ReadyService readyService,
      PersistenceServiceConfigurationRegistry configurationRegistry) {
    super(cronScheduler, scheduler, itemRegistry, safeCaller, readyService, configurationRegistry);
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

  @Override
  public void addPersistenceService(PersistenceService persistenceService) {
    super.addPersistenceService(persistenceService);
  }

  @Override
  public void removePersistenceService(PersistenceService persistenceService) {
    super.removePersistenceService(persistenceService);
  }
}
