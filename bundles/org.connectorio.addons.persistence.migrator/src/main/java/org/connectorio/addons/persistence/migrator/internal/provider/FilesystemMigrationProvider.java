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
package org.connectorio.addons.persistence.migrator.internal.provider;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.connectorio.addons.persistence.migrator.operation.Container;
import org.connectorio.addons.persistence.migrator.MigrationProvider;
import org.connectorio.addons.persistence.migrator.internal.Constants;
import org.connectorio.addons.persistence.migrator.internal.xml.Migrations;
import org.connectorio.addons.persistence.migrator.internal.xml.MigrationXmlReader;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.service.ReadyService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FilesystemMigrationProvider implements MigrationProvider {

  private final Logger logger = LoggerFactory.getLogger(FilesystemMigrationProvider.class);
  private final List<Container> migrations = new ArrayList<>();
  private final ReadyService readyService;

  @Activate
  public FilesystemMigrationProvider(@Reference ReadyService readyService) {
    this.readyService = readyService;
    File migrationsDir = new File(OpenHAB.getConfigFolder(), "migrations");
    if (migrationsDir.exists() && migrationsDir.isDirectory()) {
      File[] files = migrationsDir.listFiles(name -> name.getName().endsWith(".xml"));
      if (files == null || files.length == 0) {
        readyService.markReady(Constants.FILESYSTEM_MARKER);
        return;
      }

      MigrationXmlReader reader = new MigrationXmlReader();
      Arrays.sort(files, new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
      for (File file : files) {
        try {
          Migrations migrations = reader.readFromXML(file.toURI().toURL());
          migrations.setUID(file.getName());
          this.migrations.add(migrations);
        } catch (MalformedURLException e) {
          logger.warn("Could not read migration", e);
        }
      }
    }
    readyService.markReady(Constants.FILESYSTEM_MARKER);
  }

  @Deactivate
  void deactivate() {
    readyService.unmarkReady(Constants.FILESYSTEM_MARKER);
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<Container> listener) {

  }

  @Override
  public Collection<Container> getAll() {
    return migrations;
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<Container> listener) {

  }

}
