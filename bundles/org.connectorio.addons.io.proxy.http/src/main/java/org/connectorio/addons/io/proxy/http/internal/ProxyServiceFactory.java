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
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServiceFactory implements ManagedServiceFactory, ManagedService {

  public final static String SERVICE_PID = "org.connectorio.addons.io.proxy.http";

  public final static Pattern KEY_MASK = Pattern.compile("proxy\\.(\\w+)\\..*?");

  private final Logger logger = LoggerFactory.getLogger(ProxyServiceFactory.class);

  private final BundleContext bundleContext;
  private final HttpService httpService;
  private Map<String, Proxy> pidToPath = new ConcurrentHashMap<>();

  public ProxyServiceFactory(BundleContext bundleContext, HttpService service) {
    this.bundleContext = bundleContext;
    this.httpService = service;
  }

  @Override
  public String getName() {
    return "HTTP Proxy service factory";
  }

  @Override
  public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
    deleted(pid);

    if (properties == null) {
      return;
    }

    String path = Optional.ofNullable(properties.get(Proxy.PATH))
      .filter(value -> value instanceof String)
      .map(Object::toString)
      .orElse(null);

    if (path != null) {
      if (pidToPath.containsValue(path)) {
        throw new ConfigurationException("path", "Path " + path + " is already in use");
      }

      try {
        Proxy proxy = new Proxy(bundleContext, path, properties, httpService);
        proxy.register();

        pidToPath.put(pid, proxy);
        logger.info("Registered new proxy at {}", path);
      } catch (ServletException | NamespaceException e) {
        logger.error("Could not register service");
        throw new ConfigurationException("path", "Could not register http servlet", e);
      }
    }
  }

  @Override
  public void deleted(String pid) {
    if (pidToPath.containsKey(pid)) {
      if (pidToPath.get(pid) != null) {
        pidToPath.remove(pid).unregister();
      } else {
        pidToPath.remove(pid);
      }
    }
  }

  // variant which works with singular pid - emulates multiple PIDs with proxy.[pid].[key]=[value] syntax.
  @Override
  public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
    if (properties == null) {
      pidToPath.keySet().forEach(this::deleted);
      return;
    }

    Enumeration<String> keys = properties.keys();

    Set<String> pids = new LinkedHashSet<>();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();

      Matcher matcher = KEY_MASK.matcher(key);
      if (matcher.matches()) {
        pids.add(matcher.group(1));
      }
    }

    for (String pid : pids) {
      updated(SERVICE_PID + "-" + pid, new FilteredDictionary<>("proxy." + pid + ".", properties));
    }
  }

}
