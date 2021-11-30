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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.connectorio.addons.persistence.migrator.MigrationContext;
import org.connectorio.addons.persistence.migrator.internal.xml.ItemReference;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.connectorio.addons.persistence.migrator.operation.Statuses;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.PersistenceService;

public abstract class PatternOperation extends BaseOperation implements Operation {

  private ItemReference source;

  public PatternOperation(String sourceItem, String sourceService) {
    this.source = new ItemReference(sourceItem, sourceService);
  }

  public ItemReference getSource() {
    return source;
  }

  @Override
  public Status execute(MigrationContext context) {
    MultiItemPersistenceServicePair itemWIthPersistence = lookup(context, source);
    if (itemWIthPersistence == null) {
      return Statuses.IGNORED;
    }

    return execute(context, itemWIthPersistence.items, itemWIthPersistence.service);
  }

  protected abstract Status execute(MigrationContext context, Map<Item, Matcher> item, PersistenceService service);

  protected final MultiItemPersistenceServicePair lookup(MigrationContext context, ItemReference reference) {
    Pattern itemPattern = Pattern.compile(reference.getItem());

    Map<Item, Matcher> items = new LinkedHashMap<>();
    for (Item item : context.getItems()) {
      Matcher matcher = itemPattern.matcher(item.getName());
      if (matcher.matches()) {
        items.put(item, matcher);
      }
    }
    if (items.isEmpty()) {
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
    return new MultiItemPersistenceServicePair(items, persistenceService);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PatternOperation)) {
      return false;
    }
    PatternOperation that = (PatternOperation) o;
    return Objects.equals(getSource(), that.getSource());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSource());
  }

  static class MultiItemPersistenceServicePair {
    final Map<Item, Matcher> items;
    final PersistenceService service;

    MultiItemPersistenceServicePair(Map<Item, Matcher> items, PersistenceService service) {
      this.items = items;
      this.service = service;
    }
  }
}
