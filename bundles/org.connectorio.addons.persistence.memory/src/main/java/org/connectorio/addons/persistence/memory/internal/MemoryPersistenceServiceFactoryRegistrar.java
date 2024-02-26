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
package org.connectorio.addons.persistence.memory.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * A registrator of OSGi managed service factory used to construct new instances of memory persistence
 * service.
 */
@Component(immediate = true)
public class MemoryPersistenceServiceFactoryRegistrar {

  private final ServiceRegistration<?> registration;

  @Activate
  public MemoryPersistenceServiceFactoryRegistrar(@Reference TimeZoneProvider timeZoneProvider, BundleContext context) {
    Dictionary<String, Object> properties = new Hashtable<>();
    properties.put(Constants.SERVICE_PID, MemoryPersistenceServiceFactory.SERVICE_ID);
    registration = context.registerService(ManagedServiceFactory.class, new MemoryPersistenceServiceFactory(timeZoneProvider, context), properties);
  }

  @Deactivate
  public void deleted() {
    if (registration != null) {
      registration.unregister();
    }
  }

}
