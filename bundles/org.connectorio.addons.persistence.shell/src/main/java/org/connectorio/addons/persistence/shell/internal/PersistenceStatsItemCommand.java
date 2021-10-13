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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
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
    try {
      console.println("Default service: " + persistenceService.getDefaultId());
      for (PersistenceService service : persistenceService.getAll()) {
        console.println("Checking " + service.getId() + " persisted items");
        stats(console, service);
      }
    } catch (Exception e) {
      console.println("Error " + e.getMessage());
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-persistence-stats");
  }

  private void stats(Console console, PersistenceService service) {
    if (service instanceof QueryablePersistenceService) {
      QueryablePersistenceService persistence = (QueryablePersistenceService) service;
      Set<PersistenceItemInfo> foundInfos = persistence.getItemInfo();
      console.println("Service holds " + foundInfos.size() + " elements");

      Set<PersistenceItemInfo> infos = new TreeSet<>(Comparator.comparing(PersistenceItemInfo::getName));
      infos.addAll(foundInfos);
      console.println("Service " + service.getId() + " contains " + infos.size() + " items");
      Long counter = 0L;
      ZonedDateTime oldest = ZonedDateTime.now();
      for (PersistenceItemInfo info : infos) {
        console.println(info.getName() + " " + info.getCount() + " " + info.getEarliest() + " " + info.getLatest());
        counter += info.getCount();

        if (info.getEarliest() != null) {
          ZonedDateTime earliest = Instant.ofEpochMilli(info.getEarliest().getTime()).atZone(ZoneId.systemDefault());
          if (earliest.isBefore(oldest)) {
            oldest = earliest;
          }
        }
      }

      if (foundInfos.size() > 0) {
        console.println("");
        console.println("Total: " + counter + " database entries. Average " + (counter / foundInfos.size()) + " per table");
        Period period = Period.between(oldest.toLocalDate(), LocalDate.now());
        console.println("Oldest: " + oldest + ",  " + period.getYears() + " years, " + period.getMonths() + " months, " + period.getDays() + " days ago");
      }
    } else {
      console.println("Service " + service.getId() + " is not queryable");
    }
  }

}
