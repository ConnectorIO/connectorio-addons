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
package org.connectorio.addons.startlevel.osgi.internal;

import java.util.function.Consumer;
import org.connectorio.addons.startlevel.osgi.OsgiReadyMarker;
import org.openhab.core.service.ReadyService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Publisher for OSGi related ready markers, populates information based on registered (or disappeared) services.
 *
 * Internally tracker uses two properties:
 * - component.name - the SCR component name which identify service within publishing bundle.
 * - service.pid - persistent identifier used by services, in case if pid is an array, each value is
 * published as individual marker.
 */
@Component(immediate = true)
public class OsgiReadyMarkerPublisher implements ServiceTrackerCustomizer<Object, ServiceReference<?>> {

  private final ReadyService readyService;
  private final ServiceTracker<?, ?> serviceTracker;

  @Activate
  public OsgiReadyMarkerPublisher(@Reference ReadyService readyService, BundleContext context) throws InvalidSyntaxException {
    this.readyService = readyService;
    serviceTracker = new ServiceTracker<>(context, "*", this);
    serviceTracker.open(true);

    // mark already registered service
    ServiceReference<?>[] references = context.getServiceReferences((String) null, null);
    for (ServiceReference<?> reference : references) {
      mark(reference);
    }
  }

  @Deactivate
  public void deactivate() {
    serviceTracker.close();
  }

  @Override
  public ServiceReference<?> addingService(ServiceReference<Object> reference) {
    mark(reference);
    return reference;
  }

  @Override
  public void modifiedService(ServiceReference<Object> reference, ServiceReference<?> service) {
    unmark(reference);
    mark(service);
  }

  @Override
  public void removedService(ServiceReference<Object> reference, ServiceReference<?> service) {
    unmark(reference);
  }

  private void mark(ServiceReference<?> reference) {
    process(reference, (property) -> readyService.markReady(OsgiReadyMarker.serviceMarker(property)));
  }

  private void unmark(ServiceReference<?> reference) {
    process(reference, (property) -> readyService.unmarkReady(OsgiReadyMarker.serviceMarker(property)));
  }

  private void process(ServiceReference<?> reference, Consumer<String> consumer) {
    if (reference.getProperty("component.name") != null) {
      Object componentName = reference.getProperty("component.name");
      if (componentName instanceof String) {
        consumer.accept((String) componentName);
      }
    }
    Object servicePid = reference.getProperty(Constants.SERVICE_PID);
    if (servicePid != null) {
      if (servicePid instanceof String) {
        consumer.accept((String) servicePid);
      } else if (servicePid instanceof String[]) {
        for (String pid : (String[]) servicePid) {
          consumer.accept(pid);
        }
      }
    }
  }

}
