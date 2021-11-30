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

import org.connectorio.addons.persistence.migrator.MigrationContext;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.connectorio.addons.persistence.migrator.operation.Statuses;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.PersistenceService;

public class DeleteOperation extends ItemOperation implements Operation {

  public DeleteOperation() {
    this(null, null);
  }

  public DeleteOperation(String sourceItem, String sourceService) {
    super(sourceItem, sourceService);
  }

  @Override
  protected Status execute(MigrationContext context, Item item, PersistenceService service) {
    // operation not performed
    return Statuses.FAILURE;
  }

  public String toString() {
    return "Delete[source=" + getSource() + "]";
  }

}
