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

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.connectorio.addons.persistence.manager.internal.xml.PersistenceXmlReader;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.persistence.PersistenceManager;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceConfiguration;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class PersistenceManagerExtension implements ReadyTracker {

  private final Logger logger = LoggerFactory.getLogger(PersistenceManagerExtension.class);
  private final ReadyMarker marker = new ReadyMarker("co7io-persistence", "configure");
  private final List<PersistenceService> services = new CopyOnWriteArrayList<>();

  private final ReadyService readyService;
  private final PersistenceManager manager;

  @Activate
  public PersistenceManagerExtension(@Reference ReadyService readyService, @Reference PersistenceManager manager) {
    this.readyService = readyService;
    this.manager = manager;
    // wait for retrieval of items which should be set at the same start level of 20.
    readyService.registerTracker(this, new ReadyMarkerFilter().withType("managed").withIdentifier("item"));
  }

  @Override
  public void onReadyMarkerAdded(ReadyMarker readyMarker) {
    ExecutorService scheduler = Executors.newSingleThreadExecutor(new NamedThreadFactory("persistenceManager"));
    scheduler.submit(() -> {
      for (PersistenceService service : services) {
        PersistenceServiceConfiguration configuration = configure(service.getId());
        if (configuration != null) {
          manager.addConfig(service.getId(), configuration);
        } else {
          logger.info("No dedicated persistence configuration for service {}. It will rely on own defaults", service.getId());
        }
      }
      readyService.markReady(marker);
    });
    scheduler.shutdown();
  }

  @Override
  public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    readyService.unmarkReady(marker);
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void addPersistenceService(PersistenceService service) {
    services.add(service);
  }

  public void removePersistenceService(PersistenceService service) {
    services.remove(service);
  }

  private PersistenceServiceConfiguration configure(String serviceId) {
    File persistenceConfig = new File(OpenHAB.getConfigFolder(), serviceId + ".xml");
    if (persistenceConfig.exists()) {
      try {
        PersistenceXmlReader reader = new PersistenceXmlReader();
        logger.info("Successfully read persistence service {}.", serviceId);
        return reader.readFromXML(persistenceConfig.toURI().toURL());
      } catch (Exception e) {
        logger.error("Could not parse configuration, service will rely on its defaults!");
      }
    }
    return null;
  }

}
