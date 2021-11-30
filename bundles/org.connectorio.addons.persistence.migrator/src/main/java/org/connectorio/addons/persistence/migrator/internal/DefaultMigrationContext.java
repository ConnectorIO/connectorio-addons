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
package org.connectorio.addons.persistence.migrator.internal;

import java.util.Collection;
import org.connectorio.addons.persistence.migrator.MigrationContext;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;

public class DefaultMigrationContext implements MigrationContext {

  private final String service;
  private final ItemRegistry itemRegistry;
  private final PersistenceServiceRegistry persistenceServiceRegistry;

  public DefaultMigrationContext(String service, ItemRegistry itemRegistry, PersistenceServiceRegistry persistenceServiceRegistry) {
    this.service = service;
    this.itemRegistry = itemRegistry;
    this.persistenceServiceRegistry = persistenceServiceRegistry;
  }

  @Override
  public String getService() {
    return service;
  }

  @Override
  public Item getItem(String item) {
    return itemRegistry.get(item);
  }

  @Override
  public PersistenceService getService(String service) {
    return persistenceServiceRegistry.get(service);
  }

  @Override
  public Collection<Item> getItems() {
    return itemRegistry.getItems();
  }

}
