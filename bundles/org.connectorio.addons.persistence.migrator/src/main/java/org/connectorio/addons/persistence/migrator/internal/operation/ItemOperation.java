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
package org.connectorio.addons.persistence.migrator.internal.operation;

import java.util.Objects;
import org.connectorio.addons.persistence.migrator.MigrationContext;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.connectorio.addons.persistence.migrator.operation.Statuses;
import org.connectorio.addons.persistence.migrator.internal.xml.ItemReference;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.PersistenceService;

public abstract class ItemOperation implements Operation {

  private ItemReference source;

  public ItemOperation(String sourceItem, String sourceService) {
    this.source = new ItemReference(sourceItem, sourceService);
  }

  public ItemReference getSource() {
    return source;
  }

  @Override
  public Status execute(MigrationContext context) {
    ItemPersistenceServicePair itemWIthPersistence = lookup(context, source);
    if (itemWIthPersistence == null) {
      return Statuses.IGNORED;
    }

    return execute(context, itemWIthPersistence.item, itemWIthPersistence.service);
  }

  protected abstract Status execute(MigrationContext context, Item item, PersistenceService service);

  protected final ItemPersistenceServicePair lookup(MigrationContext context, ItemReference reference) {
    String itemName = reference.getItem();

    Item item = context.getItem(itemName);
    if (item == null) {
      return null;
    }

    String service = getSource().getService();
    if (service == null) {
      service = context.getService();
    }
    if (service == null) {
      return null;
    }

    PersistenceService persistenceService = context.getService(service);
    if (persistenceService == null) {
      return null;
    }
    return new ItemPersistenceServicePair(item, persistenceService);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ItemOperation)) {
      return false;
    }
    ItemOperation that = (ItemOperation) o;
    return getSource().equals(that.getSource());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSource());
  }

  static class ItemPersistenceServicePair {
    final Item item;
    final PersistenceService service;

    ItemPersistenceServicePair(Item item, PersistenceService service) {
      this.item = item;
      this.service = service;
    }
  }
}
