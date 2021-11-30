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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.connectorio.addons.persistence.migrator.CompositeExecutionStatus;
import org.connectorio.addons.persistence.migrator.operation.Container;
import org.connectorio.addons.persistence.migrator.MigrationManager;
import org.connectorio.addons.persistence.migrator.operation.Operation;
import org.connectorio.addons.persistence.migrator.operation.Status;
import org.connectorio.addons.persistence.migrator.operation.Statuses;
import org.openhab.core.common.registry.Registry;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultMigrationManager implements MigrationManager, ReadyTracker {

  private final ExecutorService executor = Executors.newSingleThreadExecutor((runnable) -> new Thread(runnable, "migration-manager"));
  private final Logger logger = LoggerFactory.getLogger(DefaultMigrationManager.class);
  private final Map<Container, CompositeExecutionStatus<Container>> statuses = new LinkedHashMap<>();

  private final ReadyService readyService;
  private final Registry<Container, String> migrationRegistry;
  private final ItemRegistry itemRegistry;
  private final PersistenceServiceRegistry persistenceServiceRegistry;

  private final RegistryChangeListener<Container> listener;

  @Activate
  public DefaultMigrationManager(@Reference ReadyService readyService, @Reference(target = "(migrations=true)") Registry<Container, String> migrationRegistry,
    @Reference ItemRegistry itemRegistry, @Reference PersistenceServiceRegistry persistenceServiceRegistry) {
    this.readyService = readyService;
    this.migrationRegistry = migrationRegistry;
    this.itemRegistry = itemRegistry;
    this.persistenceServiceRegistry = persistenceServiceRegistry;

    migrationRegistry.addRegistryChangeListener(listener = new RegistryChangeListener<Container>() {
      @Override
      public void added(Container element) {
        if (!statuses.containsKey(element)) {
          statuses.put(element, new MigrationExecutionStatus(Statuses.WAITING, element));
        }
      }

      @Override
      public void removed(Container element) {
        //
      }

      @Override
      public void updated(Container oldElement, Container element) {

      }
    });
    readyService.registerTracker(this, new ReadyMarkerFilter().withType(Constants.FILESYSTEM_MARKER.getType())
      .withIdentifier(Constants.FILESYSTEM_MARKER.getIdentifier()));
  }

  @Deactivate
  void deactivate() {
    migrationRegistry.removeRegistryChangeListener(listener);
    readyService.unregisterTracker(this);
    executor.shutdownNow();
  }

  @Override
  public Collection<CompositeExecutionStatus<Container>> getExecutionStatus() {
    return statuses.values();
  }

  @Override
  public boolean isAllMigrationsSucceeded() {
    for (CompositeExecutionStatus<Container> status : statuses.values()) {
      if (Statuses.FAILURE == status || Statuses.WAITING == status) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void execute() {
    for (Container migration : migrationRegistry.getAll()) {
      DefaultMigrationContext context = new DefaultMigrationContext(migration.getService(), itemRegistry, persistenceServiceRegistry);
      List<Operation> steps = migration.getSteps();
      if (steps == null || steps.isEmpty()) {
        statuses.put(migration, new MigrationExecutionStatus(Statuses.SKIPPED, migration));
        continue;
      }

      Operation lastOperation = null;
      MigrationExecutionStatus executionStatus = new MigrationExecutionStatus(Statuses.WAITING, migration);
      statuses.put(migration, executionStatus);
      try {
        boolean skip = false;
        for (Operation operation : steps) {
          if (skip) { // skip marker is set, we are in failure mode hence we just ignore all remaining operations in given migration
            executionStatus.add(operation, Statuses.IGNORED);
            continue;
          }

          lastOperation = operation;
          Status status = operation.execute(context);
          executionStatus.add(operation, status);
          if (Statuses.FAILURE == status) {
            logger.info("Marking migration {} as failed due to failure reported by step {}", migration, lastOperation);
            skip = true;
            executionStatus.failed();
          }
        }
        executionStatus.success();
      } catch (Exception e) {
        logger.error("Migration {} failed in step {}", migration.getUID(), lastOperation, e);
        executionStatus.failed();
      }
    }
  }

  @Override
  public void onReadyMarkerAdded(ReadyMarker readyMarker) {
    executor.submit(new Runnable() {
      @Override
      public void run() {
        execute();

        // we do not track migration result currently, shall we?
        readyService.markReady(Constants.MIGRATION_MARKER);
      }
    });
  }

  @Override
  public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    readyService.unmarkReady(Constants.MIGRATION_MARKER);
  }

}
