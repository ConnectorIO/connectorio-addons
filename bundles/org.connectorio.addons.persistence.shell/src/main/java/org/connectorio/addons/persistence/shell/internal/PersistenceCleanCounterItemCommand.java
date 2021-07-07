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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Operator;
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
public class PersistenceCleanCounterItemCommand extends AbstractConsoleCommandExtension {

  private final Clock clock;
  private final ItemRegistry itemRegistry;
  private final PersistenceServiceRegistry persistenceService;

  @Activate
  public PersistenceCleanCounterItemCommand(@Reference TimeZoneProvider timeZoneProvider, @Reference ItemRegistry itemRegistry,
    @Reference PersistenceServiceRegistry persistenceService) {
    super("co7io-persistence-clean-counter", "Clean counter item records");
    this.clock = Clock.system(timeZoneProvider.getTimeZone());
    this.itemRegistry = itemRegistry;
    this.persistenceService = persistenceService;
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-persistence-clean-counter itemName [from] [to]");
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
        delete(console, item, persistenceService.get("jdbc"), from, to);
      }
    }
  }

  private void delete(Console console, Item item, PersistenceService service, ZonedDateTime from, ZonedDateTime to) {
    if (service instanceof ModifiablePersistenceService) {
      ModifiablePersistenceService persistence = (ModifiablePersistenceService) service;

      int page = 1;
      int pageSize = 200;

      // fetch first reading
      ZonedDateTime latest = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
      ZonedDateTime earliest = null;

      int sum = 0;

      HistoricItem previous = null;
      List<HistoricItem> result = new ArrayList<>();
      int removed = 0;
      do {
        result = query(item, persistence, latest, page, pageSize);
        if (result.size() == 0) {
          break;
        }

        for (HistoricItem historicItem : result) {
          if (previous == null) {
            previous = historicItem;
          }

          if (malformed(previous.getState(), historicItem.getState())) {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setItemName(item.getName());
            criteria.setBeginDate(historicItem.getTimestamp());
            criteria.setEndDate(historicItem.getTimestamp());
            criteria.setOperator(Operator.EQ);
            criteria.setPageSize(1);

            console.println("Removing state " + historicItem.getState() + ", cause it is lower than " + previous.getState());
            persistence.remove(criteria);
            removed++;
          } else {
            previous = historicItem;
          }
          latest = historicItem.getTimestamp();
        }
      } while (result.size() == pageSize);
      console.println(item.getName() + " removed " + removed + " records");
    }
  }

  private boolean malformed(State past, State present) {
    if (past instanceof DecimalType && present instanceof DecimalType) {
      BigDecimal pastValue = ((DecimalType) past).toBigDecimal();
      BigDecimal presentValue = ((DecimalType) present).toBigDecimal();
      return presentValue.compareTo(pastValue) < 0;
    }

    if (past instanceof QuantityType && present instanceof QuantityType) {
      BigDecimal pastValue = ((QuantityType<?>) past).toBigDecimal();
      BigDecimal presentValue = ((QuantityType<?>) present).toBigDecimal();
      return presentValue.compareTo(pastValue) < 0;
    }

    return false;
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
