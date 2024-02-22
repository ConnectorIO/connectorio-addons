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

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * A handy way to provide tooling for persistence services within configuration windows.
 */
@Component(service = ConfigOptionProvider.class)
public class PersistenceConfigOptionsProvider implements ConfigOptionProvider {

  private final String PARAMETER_PERSISTENCE_TARGET = "persistenceTarget";
  private final String PARAMETER_PERSISTENCE_TARGETS = "persistenceTargets";
  private final String PARAMETER_PERSISTENCE_SOURCE = "persistenceSource";
  private final String PARAMETER_PERSISTENCE_SOURCES = "persistenceSources";

  private final Set<String> properties = new HashSet<>(Arrays.asList(
    PARAMETER_PERSISTENCE_TARGET,
    PARAMETER_PERSISTENCE_TARGETS,
    PARAMETER_PERSISTENCE_SOURCE,
    PARAMETER_PERSISTENCE_SOURCES
  ));

  private final PersistenceServiceRegistry serviceRegistry;

  @Activate
  public PersistenceConfigOptionsProvider(@Reference PersistenceServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  @Override
  public Collection<ParameterOption> getParameterOptions(URI uri, String param, String context, Locale locale) {
    String configDescriptor = uri.toString();
    if ((configDescriptor.contains("connectorio") || configDescriptor.contains("co7io") || configDescriptor.contains("metadata:")) && properties.contains(param)) {
      Set<ParameterOption> options = new HashSet<>();
      for (PersistenceService service : serviceRegistry.getAll()) {
        options.add(new ParameterOption(service.getId(), service.getLabel(locale)));
      }
      return options;
    }
    return null;
  }
}