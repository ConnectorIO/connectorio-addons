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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.connectorio.addons.persistence.shell.internal.item.StubItem;
import org.connectorio.addons.persistence.shell.internal.item.StubNumberItem;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which gets JDBC persistence and does period calculation.
 *
 * It does pretty much the same thing as the automation stuff with the difference that it does it for all items at once.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class PersistenceCopyItemCommand extends AbstractConsoleCommandExtension {

  private final Clock clock;
  private final ItemRegistry itemRegistry;
  private final PersistenceServiceRegistry persistenceService;

  @Activate
  public PersistenceCopyItemCommand(@Reference TimeZoneProvider timeZoneProvider, @Reference ItemRegistry itemRegistry, @Reference PersistenceServiceRegistry persistenceService) {
    super("co7io-persistence-copy", "Copy recorded data between items");
    this.clock = Clock.system(timeZoneProvider.getTimeZone());
    this.itemRegistry = itemRegistry;
    this.persistenceService = persistenceService;
  }

  @Override
  public void execute(String[] args, Console console) {
    if (args.length != 2) {
      console.println("Specify from and to item only.");
      return;
    }

    String from = args[0];
    String to = args[1];

    try {
      Item fromItem = itemRegistry.getItem(from);
      Item toItem = itemRegistry.getItem(to);
      copy(console, fromItem, toItem, persistenceService.get("jdbc"));
    } catch (ItemNotFoundException e) {
      console.println("Item not found");
      e.printStackTrace();
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-persistence-copy sourceItem targetItem");
  }

  private void copy(Console console, Item from, Item to, PersistenceService service) {
    if (service instanceof ModifiablePersistenceService) {
      ModifiablePersistenceService persistence = (ModifiablePersistenceService) service;

      int page = 0;
      int pageSize = 200;

      // fetch first reading
      ZonedDateTime latest = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());

      int sum = 0;

      List<HistoricItem> result = new ArrayList<>();
      do {
        result = query(from, persistence, latest, page, pageSize);
        if (result.size() == 0) {
          break;
        }

        HistoricItem historicItem = result.get(result.size() - 1);
        latest = historicItem.getTimestamp();
        for (HistoricItem item : result) {
          persistence.store(createStub(to, item.getState()), item.getTimestamp(), item.getState());
        }
        sum += result.size();
      } while (result.size() == pageSize);

      if (sum == 0) {
        return;
      }
      console.println("Copied " + sum + " entries to " + to.getName());
    }
  }

  private Item createStub(Item item, State state) {
    if (item instanceof NumberItem) {
      return new StubNumberItem((NumberItem) item, state) {
        @Override
        public String getType() {
          return item.getType();
        }
      };
    }
    return new StubItem(item, state);
  }

  private List<HistoricItem> query(Item from, ModifiablePersistenceService persistence, ZonedDateTime latest, int page,
    int pageSize) {
    FilterCriteria c = new FilterCriteria();
    c.setItemName(from.getName());
    c.setBeginDate(latest);
    c.setOrdering(Ordering.ASCENDING);
    c.setPageNumber(page);
    c.setPageSize(pageSize);

    List<HistoricItem> items = new ArrayList<>();
    persistence.query(c).forEach(items::add);

    return items;
  }

}
