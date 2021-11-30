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
import java.util.Date;
import org.connectorio.addons.persistence.migrator.MigrationContext;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.connectorio.addons.persistence.migrator.operation.Statuses;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceService;

/**
 * Operation which copies data from one item to another.
 */
public class CopyOperation extends ItemPairOperation implements Operation {

  public CopyOperation() {
    this(null, null, null, null);
  }

  public CopyOperation(String sourceItem, String sourceService, String targetItem, String targetService) {
    super(sourceService, sourceItem, targetItem, targetService);
  }

  @Override
  protected Status execute(MigrationContext context, Item item, PersistenceService service) {
    if (!(service instanceof ModifiablePersistenceService)) {
      return Statuses.FAILURE;
    }
    ModifiablePersistenceService sourceService = (ModifiablePersistenceService) service;

    ItemPersistenceServicePair target = lookup(context, getTarget());
    if (target == null) {
      return Statuses.FAILURE;
    }

    if (!(target.service instanceof ModifiablePersistenceService)) {
      return Statuses.FAILURE;
    }

    Item targetItem = target.item;
    ModifiablePersistenceService targetService = (ModifiablePersistenceService) service;
    for (HistoricItem state : sourceService.query(new FilterCriteria().setItemName(item.getName()))) {
      ZonedDateTime timestamp = state.getTimestamp();
      Date date = new Date(timestamp.toInstant().toEpochMilli());
      targetService.store(targetItem, date, state.getState());
    }

    return Statuses.SUCCESS;
  }

  public String toString() {
    return "Copy[source=" + getSource() + ", target=" + getTarget() + "]";
  }

}
