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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.connectorio.addons.persistence.migrator.MigrationContext;
import org.connectorio.addons.persistence.migrator.internal.item.StubItem;
import org.connectorio.addons.persistence.migrator.internal.xml.ItemReference;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.connectorio.addons.persistence.migrator.operation.Statuses;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Operation which copies data from items with given name pattern to another.
 */
public class CopyPatternOperation extends PatternOperation implements Operation {

  private ItemReference target;

  public CopyPatternOperation() {
    this(null, null, null, null);
  }

  public CopyPatternOperation(String sourceItem, String sourceService, String targetItem, String targetService) {
    super(sourceItem, sourceService);
    this.target = new ItemReference(targetItem, targetService);
  }

  public ItemReference getTarget() {
    return target;
  }

  @Override
  protected Status execute(MigrationContext context, Map<Item, Matcher> items, PersistenceService service) {
    if (!(service instanceof ModifiablePersistenceService)) {
      return Statuses.FAILURE;
    }
    ModifiablePersistenceService sourceService = (ModifiablePersistenceService) service;

    String targetServiceId = getTarget().getService();
    if (targetServiceId == null) {
      targetServiceId = context.getService();
    }
    PersistenceService destinationService = context.getService(targetServiceId);
    if (!(destinationService instanceof ModifiablePersistenceService)) {
      return Statuses.FAILURE;
    }
    ModifiablePersistenceService targetService = (ModifiablePersistenceService) destinationService;

    Pattern pattern = Pattern.compile("\\$(\\d+)");

    for (Entry<Item, Matcher> entry : items.entrySet()) {
      String targetItemPattern = getTarget().getItem();
      // look for $n references and replace it with values gathered from source item
      Matcher patternMatcher = pattern.matcher(targetItemPattern);
      while (patternMatcher.find()) {
        int start = patternMatcher.start();
        int end = patternMatcher.end();
        int sourceRef = Integer.parseInt(targetItemPattern.substring(start + 1, end));

        if (sourceRef > entry.getValue().groupCount()) {
          getLogger().warn("Ignore migration of data from {}. Patterns {} and {} are not balanced", entry.getKey().getName(), getSource().getItem(), targetItemPattern);
          return Statuses.FAILURE;
        }

        // keep looking for remaining occurrences!
        targetItemPattern = patternMatcher.replaceFirst(entry.getValue().group(sourceRef));
        patternMatcher = pattern.matcher(targetItemPattern);
      }

      Item item = context.getItem(targetItemPattern);
      if (item != null) {
        getLogger().info("Copy data from {} to {}", entry.getKey().getName(), item.getName());
        copy(targetService, item, sourceService, entry.getKey());
      }
    }

    return Statuses.SUCCESS;
  }

  private void copy(ModifiablePersistenceService targetService, Item targetItem, ModifiablePersistenceService sourceService,
    Item sourceItem) {
    for (HistoricItem historic : sourceService.query(new FilterCriteria().setItemName(sourceItem.getName()))) {
      State state = historic.getState();
      if (state == UnDefType.UNDEF || state == UnDefType.NULL) {
        continue;
      }
      ZonedDateTime timestamp = historic.getTimestamp();
      targetService.store(new StubItem(targetItem, state), Date.from(timestamp.toInstant()), state);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CopyPatternOperation)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    CopyPatternOperation that = (CopyPatternOperation) o;
    return Objects.equals(getTarget(), that.getTarget());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getTarget());
  }
}
