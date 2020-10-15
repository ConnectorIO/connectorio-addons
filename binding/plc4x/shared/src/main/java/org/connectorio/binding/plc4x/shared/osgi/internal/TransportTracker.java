/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
 */
package org.connectorio.binding.plc4x.shared.osgi.internal;

import java.util.List;
import org.apache.plc4x.java.spi.transport.Transport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A transport tracker which registers its class loaders as source for class lookups for PlcDriverManager.
 */
public class TransportTracker implements ServiceTrackerCustomizer<Transport, ClassLoader> {

  private final BundleContext context;
  private final List<ClassLoader> wirings;

  public TransportTracker(BundleContext context, List<ClassLoader> wirings) {
    this.context = context;
    this.wirings = wirings;
  }

  @Override
  public ClassLoader addingService(ServiceReference<Transport> reference) {
    final ClassLoader loader = reference.getBundle().adapt(BundleWiring.class).getClassLoader();
    wirings.add(loader);
    return loader;
  }

  @Override
  public void modifiedService(ServiceReference<Transport> reference, ClassLoader service) {
  }

  @Override
  public void removedService(ServiceReference<Transport> reference, ClassLoader service) {
    context.ungetService(reference);
    wirings.remove(service);
  }
}
