/*
 * Copyright (C) 2019-2024 ConnectorIO Sp. z o.o.
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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.connectorio.addons.persistence.manager.PersistenceMarker;
import org.connectorio.addons.persistence.manager.internal.xml.PersistenceXmlReader;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.registry.PersistenceServiceConfiguration;
import org.openhab.core.persistence.registry.PersistenceServiceConfigurationProvider;
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
public class PersistenceManagerExtension implements ReadyTracker, PersistenceServiceConfigurationProvider {

  private final Logger logger = LoggerFactory.getLogger(PersistenceManagerExtension.class);
  private final ReadyMarker marker = new ReadyMarker("co7io-persistence", "configure");
  private final Map<String, PersistenceServiceConfiguration> configurations = new ConcurrentHashMap<>();

  private Set<ProviderChangeListener<PersistenceServiceConfiguration>> listeners = new CopyOnWriteArraySet<>();

  private final ReadyService readyService;

  @Activate
  public PersistenceManagerExtension(@Reference ReadyService readyService) {
    this.readyService = readyService;
    // wait for retrieval of items which should be set at the same start level of 20.
    readyService.registerTracker(this, new ReadyMarkerFilter()
      .withType(PersistenceMarker.PERSISTENCE_SERVICES.getType())
      .withIdentifier(PersistenceMarker.PERSISTENCE_SERVICES.getIdentifier())
    );
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<PersistenceServiceConfiguration> listener) {
    this.listeners.add(listener);
  }

  @Override
  public Collection<PersistenceServiceConfiguration> getAll() {
    return configurations.values();
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<PersistenceServiceConfiguration> listener) {
    this.listeners.remove(listener);
  }

  @Override
  public void onReadyMarkerAdded(ReadyMarker readyMarker) {
    ExecutorService scheduler = Executors.newSingleThreadExecutor(new NamedThreadFactory("persistenceManager"));
    scheduler.submit(() -> {
      for (String serviceId : configurations.keySet()) {
        PersistenceServiceConfiguration configuration = configure(serviceId);
        if (configuration != null) {
          listeners.forEach(listener -> listener.added(this, configuration));
        } else {
          logger.info("No dedicated persistence configuration for service {}. It will rely on own defaults", serviceId);
        }
      }
      readyService.markReady(PersistenceMarker.PERSISTENCE_CONFIGURE);
    });
    scheduler.shutdown();
  }

  @Override
  public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    readyService.unmarkReady(PersistenceMarker.PERSISTENCE_CONFIGURE);
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  public void addPersistenceService(PersistenceService service) {
    PersistenceServiceConfiguration configuration = null;
    if (configurations.containsKey(service.getId())) {
      configuration = configurations.remove(service.getId());
    }
    configurations.put(service.getId(), configuration);
  }

  public void removePersistenceService(PersistenceService service) {
    configurations.remove(service.getId());
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
