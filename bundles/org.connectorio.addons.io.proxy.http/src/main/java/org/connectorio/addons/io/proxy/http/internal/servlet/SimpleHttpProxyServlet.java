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
package org.connectorio.addons.io.proxy.http.internal.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import org.connectorio.addons.io.proxy.http.RewriteCustomizer;
import org.connectorio.addons.io.proxy.http.internal.customizer.NoopRewriteCustomizer;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.AsyncMiddleManServlet;

public class SimpleHttpProxyServlet extends AsyncMiddleManServlet {

  private final String host;
  private final Integer port;
  private final String rewrite;
  private final RewriteCustomizer customizer;

  public SimpleHttpProxyServlet(String host, Integer port, String rewrite) {
    this(host, port, rewrite, new NoopRewriteCustomizer());
  }

  public SimpleHttpProxyServlet(String host, Integer port, String rewrite, RewriteCustomizer customizer) {
    this.host = host;
    this.port = port;
    this.rewrite = rewrite;
    this.customizer = customizer;
  }

  @Override
  protected void addProxyHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
    customizer.copyHeaders(
      () -> {super.copyRequestHeaders(clientRequest, proxyRequest); return null;},
      () -> {super.addProxyHeaders(clientRequest, proxyRequest); return null;},
      clientRequest, proxyRequest
    );
  }

  @Override
  protected void copyRequestHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
    // shifted to customizer
  }

  @Override
  protected String rewriteTarget(HttpServletRequest clientRequest) {
    URL url = null;
    try {
      url = new URL("http://" + host + (port != null ? ":" + port : ""));

      if (!validateDestination(url.getHost(), url.getPort() == -1 ? url.getDefaultPort() : url.getPort())) {
        return null;
      }
    } catch (MalformedURLException e) {
      _log.warn("Malformed destination", e);
      return null;
    }

    StringBuilder destination = new StringBuilder(url.toString());
    destination.append(rewrite);

    String path = clientRequest.getPathInfo();
    if (path != null) {
      destination.append(path);
    }

    String query = clientRequest.getQueryString();
    if (query != null) {
      destination.append("?").append(query);
    }

    return destination.toString();
  }

}
