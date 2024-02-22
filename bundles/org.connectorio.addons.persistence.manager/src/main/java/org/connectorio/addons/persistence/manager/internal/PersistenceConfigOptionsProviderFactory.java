/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
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

import org.connectorio.addons.persistence.manager.PersistenceMarker;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Factory of {@link ConfigOptionProvider} which provide options pointing available persistence
 * services.
 */
@Component(immediate = true)
public class PersistenceConfigOptionsProviderFactory implements ReadyTracker {

  private final ReadyService readyService;
  private final PersistenceServiceRegistry persistenceRegistry;
  private final BundleContext context;
  private ServiceRegistration<?> registration;

  @Activate
  public PersistenceConfigOptionsProviderFactory(@Reference ReadyService readyService,
    @Reference PersistenceServiceRegistry persistenceRegistry, BundleContext context) {
    this.readyService = readyService;
    this.persistenceRegistry = persistenceRegistry;
    this.context = context;

    this.readyService.registerTracker(this, new ReadyMarkerFilter()
        .withType(PersistenceMarker.PERSISTENCE_SERVICES.getType())
        .withIdentifier(PersistenceMarker.PERSISTENCE_SERVICES.getIdentifier())
    );
  }

  @Override
  public void onReadyMarkerAdded(ReadyMarker readyMarker) {
    registration = context.registerService(ConfigOptionProvider.class, new PersistenceConfigOptionsProvider(persistenceRegistry), null);
  }

  @Override
  public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    if (registration != null) {
      registration.unregister();
    }
  }

}
