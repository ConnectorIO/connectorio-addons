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
package org.connectorio.addons.io.proxy.http.internal;

import org.connectorio.addons.io.proxy.http.internal.tracker.CompositeRegistration;
import org.connectorio.addons.io.proxy.http.internal.tracker.HttpServiceTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

  private final String PID = System.getProperty("org.connectorio.proxy.pid", "org.connectorio.addons.io.proxy.http");
  private ServiceTracker<HttpService, CompositeRegistration> tracker;

  @Override
  public void start(BundleContext context) throws Exception {
    tracker = new ServiceTracker<>(context, HttpService.class, new HttpServiceTracker(context, PID));
    tracker.open();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    if (tracker != null) {
      tracker.close();
      tracker = null;
    }
  }

}
