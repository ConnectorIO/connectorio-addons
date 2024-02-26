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

import java.util.AbstractMap.SimpleEntry;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Managed Service factory which creates a new instance of persistence service for each configuration
 * matching 'org.connectorio.addons.persistence.memory-*'.
 *
 * The registered service will have properties needed to get detected and configured through UI.
 */
public class MemoryPersistenceServiceFactory implements ManagedServiceFactory {

  public final static String SERVICE_ID = "org.connectorio.addons.persistence.memory";

  private final Map<String, Entry<ServiceRegistration<?>, MemoryPersistenceService>> registrations = new ConcurrentHashMap<>();

  private final Map<String, String> idMapping = new ConcurrentHashMap<>();
  private final TimeZoneProvider timeZoneProvider;
  private final BundleContext context;

  public MemoryPersistenceServiceFactory(TimeZoneProvider timeZoneProvider, BundleContext context) {
    this.timeZoneProvider = timeZoneProvider;
    this.context = context;
  }

  @Override
  public String getName() {
    return "Memory Persistence Service Factory";
  }

  @Override
  public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
    String id = ("" + properties.get("id")).trim();

    if (id.isEmpty()) {
      throw new ConfigurationException("id", "Invalid value - must be string");
    }

    if (!idMapping.containsKey(id) && registrations.containsKey(pid)) {
      // service got a new identifier, phase out old registration
      deleted(pid);
    }

    MemoryPersistenceService service;
    if (registrations.containsKey(pid)) {
      service = registrations.get(pid).getValue();
    } else {
      service = new MemoryPersistenceService(id, timeZoneProvider);
    }

    Map<String, Object> config = new HashMap<>();
    Enumeration<String> keys = properties.keys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      config.put(key, properties.get(key));
    }
    try {
      service.update(config);
    } catch (Exception e) {

    }

    if (!registrations.containsKey(pid)) {
      Dictionary<String, Object> props = new Hashtable<>();
      props.put("service.config.label", "Memory Persistence Configuration (" + id + ")");
      //props.put("service.config.factory", true);
      props.put("service.config.category", "ConnectorIO Gateway");
      props.put("service.config.description.uri", "connectorio:memory-persistence-service");
      props.put(Constants.SERVICE_PID, pid);

      registrations.put(pid, new SimpleEntry<>(context.registerService(new String[] {
        ModifiablePersistenceService.class.getName(),
        PersistenceService.class.getName(),
        QueryablePersistenceService.class.getName(),
        ModifiablePersistenceService.class.getName()
      }, service, props), service));
      idMapping.put(id, pid);
    }
  }

  @Override
  public void deleted(String pid) {
    if (registrations.containsKey(pid)) {
      registrations.get(pid).getKey().unregister();
    }
  }

}
