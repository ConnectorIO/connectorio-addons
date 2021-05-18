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
package org.connectorio.addons.io.proxy.http.internal;

import java.util.Dictionary;
import java.util.Optional;
import javax.servlet.ServletException;
import org.connectorio.addons.io.proxy.http.RewriteCustomizer;
import org.connectorio.addons.io.proxy.http.internal.customizer.NamedReferenceRewriteCustomizer;
import org.connectorio.addons.io.proxy.http.internal.customizer.NoopRewriteCustomizer;
import org.connectorio.addons.io.proxy.http.internal.servlet.SimpleHttpProxyServlet;
import org.eclipse.jetty.proxy.AbstractProxyServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class Proxy {

  public static final String PATH = "path";
  public static final String HOST = "host";
  public static final String PORT = "port";
  public static final String REWRITE = "rewrite";
  public static final String CUSTOMIZER = "customizer";


  private final BundleContext bundleContext;
  private final String path;
  private final Dictionary<String, ?> properties;
  private final HttpService httpService;

  public Proxy(BundleContext context, String path, Dictionary<String, ?> properties, HttpService httpService) {
    this.bundleContext = context;
    this.path = path;
    this.properties = properties;
    this.httpService = httpService;
  }

  public void register() throws ServletException, NamespaceException {
    String host = (String) properties.get(HOST);
    Integer port = Optional.ofNullable(properties.get(PORT))
      .map(Object::toString)
      .map(str -> {
        try {
          return Integer.parseInt(str);
        } catch (NumberFormatException e) {
          return null;
        }
      }).orElse(null);
    String rewrite = Optional.ofNullable(properties.get(REWRITE))
      .map(Object::toString)
      .orElse("/");

    RewriteCustomizer customizer = Optional.ofNullable(properties.get(CUSTOMIZER))
      .map(Object::toString)
      .map(name -> {
        if (NoopRewriteCustomizer.DEFAULT_CUSTOMIZER_NAME.equals(name)) {
          return new NoopRewriteCustomizer();
        }
        return new NamedReferenceRewriteCustomizer(bundleContext, name);
      }).orElseGet(NoopRewriteCustomizer::new);

    AbstractProxyServlet proxyServlet = new SimpleHttpProxyServlet(host, port, rewrite, customizer);
    httpService.registerServlet(path, proxyServlet, properties, httpService.createDefaultHttpContext());
  }

  public void unregister() {
    httpService.unregister(path);

  }
}
