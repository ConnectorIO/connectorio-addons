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
package org.connectorio.addons.profile.internal;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeRegistry;
import org.openhab.core.thing.profiles.StateProfileType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Currently brings no real value..
 */
//@Component(service = ConfigOptionProvider.class)
public class ProfileConfigOptionProvider implements ConfigOptionProvider {

  private static final String CONFIG_URI = "profile:connectorio:profiles";
  private static final String PROFILES_OPTION = "profiles";
  private final ProfileTypeRegistry registry;
  private Set<ProfileFactory> profileFactories;

  @Activate
  public ProfileConfigOptionProvider(@Reference ProfileTypeRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Collection<ParameterOption> getParameterOptions(URI uri, String param, String context, Locale locale) {
    if (CONFIG_URI.equals(uri.toString()) && PROFILES_OPTION.equals(param)) {
      Set<ParameterOption> options = new HashSet<>();
      for (ProfileType type : registry.getProfileTypes(locale)) {
        if (ConnectorioProfiles.PROFILE.equals(type.getUID())) {
          // remove chain from options
          continue;
        }
        if (type instanceof StateProfileType) {
          options.add(new ParameterOption(type.getUID().getAsString(), type.getLabel()));
        }
      }
      return options;
    }
    return null;
  }
}
