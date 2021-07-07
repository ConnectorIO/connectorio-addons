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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
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
public class PersistenceStatsItemCommand extends AbstractConsoleCommandExtension {

  private final ItemRegistry itemRegistry;
  private final PersistenceServiceRegistry persistenceService;

  @Activate
  public PersistenceStatsItemCommand(@Reference TimeZoneProvider timeZoneProvider, @Reference ItemRegistry itemRegistry, @Reference PersistenceServiceRegistry persistenceService) {
    super("co7io-persistence-stats", "Show persistence statistics for items");
    this.itemRegistry = itemRegistry;
    this.persistenceService = persistenceService;
  }

  @Override
  public void execute(String[] args, Console console) {
    TreeSet<Item> items = new TreeSet<>(Comparator.comparing(Item::getName));
    items.addAll(itemRegistry.getAll());
    for (Item item : items) {
      stats(console, item, persistenceService.get("jdbc"));
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-persistence-stats");
  }

  private void stats(Console console, Item from, PersistenceService service) {
    if (service instanceof ModifiablePersistenceService) {
      ModifiablePersistenceService persistence = (ModifiablePersistenceService) service;

      int page = 1;
      int pageSize = 200;

      // fetch first reading
      ZonedDateTime latest = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
      ZonedDateTime earliest = null;

      int sum = 0;

      List<HistoricItem> result = new ArrayList<>();
      do {
        result = query(from, persistence, latest, page, pageSize);
        if (result.size() == 0) {
          break;
        }

        HistoricItem historicItem = result.get(0);
        earliest = earliest == null ? historicItem.getTimestamp() : earliest;

        historicItem = result.get(result.size() - 1);
        latest = historicItem.getTimestamp();
        sum += result.size();
      } while (result.size() == pageSize);
      if (sum == 0) {
        console.println(from.getName() + " 0");
        return;
      }
      console.println(from.getName() + " " + sum + " " + earliest.toLocalDateTime() + " " + latest.toLocalDateTime());
    }
  }

  private List<HistoricItem> query(Item from, ModifiablePersistenceService persistence, ZonedDateTime latest, int page,
    int pageSize) {
    FilterCriteria c = new FilterCriteria();
    c.setItemName(from.getName());
    c.setBeginDate(latest);
    c.setOrdering(Ordering.ASCENDING);
    c.setPageNumber(1);
    c.setPageSize(pageSize);

    List<HistoricItem> items = new ArrayList<>();
    persistence.query(c).forEach(items::add);

    return items;
  }

}
