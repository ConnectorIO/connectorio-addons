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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which gets JDBC persistence and does period calculation.
 *
 * It does pretty much the same thing as the automation stuff with the difference that it does it for all items at once.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class PersistenceDeleteItemDataCommand extends AbstractConsoleCommandExtension {

  private final TimeZoneProvider timeZoneProvider;
  private final ItemRegistry itemRegistry;
  private final PersistenceServiceRegistry persistenceService;

  @Activate
  public PersistenceDeleteItemDataCommand(@Reference TimeZoneProvider timeZoneProvider, @Reference ItemRegistry itemRegistry, @Reference PersistenceServiceRegistry persistenceService) {
    super("co7io-persistence-delete-data", "Remove recorded data for given item");
    this.timeZoneProvider = timeZoneProvider;
    this.itemRegistry = itemRegistry;
    this.persistenceService = persistenceService;
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-persistence-delete-data itemNameOrPattern [from] [to]");
  }

  @Override
  public void execute(String[] args, Console console) {
    ZonedDateTime from = null;
    if (args.length == 2) {
      // from date
      TemporalAccessor parse = DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(args[1]);
      from = ZonedDateTime.from(parse);
    }

    ZonedDateTime to = null;
    if (args.length == 3) {
      // from and to date
      TemporalAccessor parse = DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(args[2]);
      to = ZonedDateTime.from(parse);
    }

    Pattern pattern = Pattern.compile(args[0]);

    TreeSet<Item> items = new TreeSet<>(Comparator.comparing(Item::getName));
    items.addAll(itemRegistry.getAll());
    for (Item item : items) {
      if (pattern.matcher(item.getName()).matches()) {
        delete(console, item.getName(), persistenceService.get("jdbc"), from, to);
      }
    }
  }

  private void delete(Console console, String item, PersistenceService service, ZonedDateTime from, ZonedDateTime to) {
    if (service instanceof ModifiablePersistenceService) {
      ModifiablePersistenceService persistence = (ModifiablePersistenceService) service;

      FilterCriteria criteria = new FilterCriteria();
      criteria.setBeginDate(from);
      criteria.setEndDate(to);
      criteria.setItemName(item);
      boolean success = persistence.remove(criteria);

      if (success) {
        console.println("Item " + item + " data was removed");
        return;
      }
      console.println("Failed to remove " + item + " data");
    }
  }

}
