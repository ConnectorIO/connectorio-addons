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
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.io.proxy.http.internal.tracker;

import java.util.Collections;
import java.util.Hashtable;
import org.connectorio.addons.io.proxy.http.internal.ProxyServiceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class HttpServiceTracker implements ServiceTrackerCustomizer<HttpService, CompositeRegistration> {

  private final BundleContext context;
  private final String configurationPid;

  public HttpServiceTracker(BundleContext context, String configurationPid) {
    this.context = context;
    this.configurationPid = configurationPid;
  }

  @Override
  public CompositeRegistration addingService(ServiceReference<HttpService> reference) {
    HttpService service = context.getService(reference);

    return new CompositeRegistration(
      context.registerService(
        ManagedServiceFactory.class,
        new ProxyServiceFactory(context, service),
        new Hashtable<>(Collections.singletonMap("service.pid", configurationPid))
      ),
      context.registerService(
        ManagedService.class,
        new ProxyServiceFactory(context, service),
        new Hashtable<>(Collections.singletonMap("service.pid", configurationPid))
      )
    );
  }

  @Override
  public void modifiedService(ServiceReference<HttpService> reference, CompositeRegistration service) {

  }

  @Override
  public void removedService(ServiceReference<HttpService> reference, CompositeRegistration service) {
    service.unregister();
  }

}
