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
package org.connectorio.addons.persistence.shell.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceItemConfiguration;
import org.openhab.core.persistence.config.PersistenceAllConfig;
import org.openhab.core.persistence.config.PersistenceConfig;
import org.openhab.core.persistence.config.PersistenceGroupConfig;
import org.openhab.core.persistence.config.PersistenceItemConfig;
import org.openhab.core.persistence.filter.PersistenceFilter;
import org.openhab.core.persistence.registry.PersistenceServiceConfiguration;
import org.openhab.core.persistence.registry.PersistenceServiceConfigurationRegistry;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which gets persistence services and prints their config.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class PersistenceStrategyCommand extends AbstractConsoleCommandExtension {

  private final ItemRegistry itemRegistry;
  private final PersistenceServiceConfigurationRegistry manager;

  @Activate
  public PersistenceStrategyCommand(@Reference ItemRegistry itemRegistry, @Reference PersistenceServiceConfigurationRegistry manager) {
    super("co7io-persistence-strategy", "Show persistence statistics for items");
    this.itemRegistry = itemRegistry;
    this.manager = manager;
  }

  @Override
  public void execute(String[] args, Console console) {
    TreeSet<Item> items = new TreeSet<>(Comparator.comparing(Item::getName));
    items.addAll(itemRegistry.getAll());

    for (PersistenceServiceConfiguration config : get(manager)) {
      console.println("service  '" + config.getUID() + "'");
      for (PersistenceItemConfiguration itemConfig : config.getConfigs()) {
        console.println("  alias '" + itemConfig.alias() + "'");
        console.println("  - includes:");
        for (PersistenceConfig itemCfg : itemConfig.items()) {
          print(console, "    ", itemCfg);
        }
        List<PersistenceStrategy> strategies = itemConfig.strategies();
        console.println("  - strategies:");
        for (PersistenceStrategy strategy : strategies) {
          print(console, "    ", strategy);
        }
        console.println("  - excludes:");
        List<PersistenceFilter> filters = itemConfig.filters();
        for (PersistenceFilter filter : filters) {
          console.println("    " + filter);
        }
      }
      console.println("  - defaults:");
      for (PersistenceStrategy strategy : config.getDefaults()) {
        console.println("  " + strategy);
      }
      console.println("  - strategies:");
      for (PersistenceStrategy strategy : config.getStrategies()) {
        print(console, "  ", strategy);
      }
    }
  }

  void print(Console console, String prefix, PersistenceStrategy strategy) {
    console.println(prefix + strategy);
  }

  void print(Console console, String prefix, PersistenceConfig config) {
    if (config instanceof PersistenceAllConfig) {
      console.println(prefix + "* (all items)");
    } else if (config instanceof PersistenceGroupConfig) {
      console.println(prefix + ((PersistenceGroupConfig) config).getGroup() + "* (group members)");
    } else if (config instanceof PersistenceItemConfig) {
      console.println(prefix + ((PersistenceItemConfig) config).getItem() + " (item)");
    } else {
      console.println(prefix + config + "(unknown)");
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-persistence-strategy");
  }

  static Collection<PersistenceServiceConfiguration> get(PersistenceServiceConfigurationRegistry manager) {
    return manager.getAll();
  }

}
