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
package org.connectorio.addons.automation.calculation.shell.internal;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.connectorio.chrono.Period;
import org.connectorio.chrono.shared.FuturePeriodCalculator;
import org.connectorio.chrono.shared.PastPeriodCalculator;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
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
import org.openhab.core.persistence.QueryablePersistenceService;
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
public class CalculationConsoleCommandExtension extends AbstractConsoleCommandExtension {

  private final Clock clock;
  private final ItemRegistry itemRegistry;
  private final PersistenceServiceRegistry persistenceService;

  @Activate
  public CalculationConsoleCommandExtension(@Reference TimeZoneProvider timeZoneProvider, @Reference ItemRegistry itemRegistry, @Reference PersistenceServiceRegistry persistenceService) {
    super("co7io-automation-calculation", "Fire calculations for found items");
    this.clock = Clock.system(timeZoneProvider.getTimeZone());
    this.itemRegistry = itemRegistry;
    this.persistenceService = persistenceService;
  }

  @Override
  public void execute(String[] args, Console console) {
    Collection<Item> items = itemRegistry.getAll();
    for (Item item : items) {
      if (item.getName().endsWith("_PreviousDay")) {
        calculateDayUsage(console, item, "_PreviousDay", Period.DAY, (date) -> date.plusDays(1));
      }
      if (item.getName().endsWith("_PreviousMonth")) {
        calculateDayUsage(console, item, "_PreviousMonth", Period.MONTH, (date) -> date.plusMonths(1));
      }
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("TODO");
  }

  private void calculateDayUsage(Console console, Item item, String suffix, org.connectorio.chrono.Period period,
    Function<ZonedDateTime, ZonedDateTime> incrementer) {
    String baseItem = item.getName().substring(0, item.getName().length() - suffix.length());
    Item calculationBase = itemRegistry.get(baseItem);
    if (calculationBase != null) {
      PersistenceService service = persistenceService.get("jdbc");
      if (service instanceof ModifiablePersistenceService) {
        ModifiablePersistenceService persistence = (ModifiablePersistenceService) service;

        console.println("Attempt to re-calculate item " + item.getName() + " from " + baseItem + " in period " + period);

        // fetch first reading
        FilterCriteria c = new FilterCriteria();
        c.setItemName(baseItem);
        c.setOrdering(Ordering.ASCENDING);
        c.setPageNumber(1);
        c.setPageSize(1);

        Iterator<HistoricItem> result = persistence.query(c).iterator();
        if (result.hasNext()) {
          HistoricItem historicItem = result.next();
          ZonedDateTime earliest = historicItem.getTimestamp();
          Instant now = clock.instant();
          int pastPeriod = 0;

          do {
            ZonedDateTime fromTime = new PastPeriodCalculator(Clock.fixed(earliest.toInstant(), earliest.getZone()),
              period).calculate();
            ZonedDateTime toTime = new FuturePeriodCalculator(Clock.fixed(fromTime.toInstant(), fromTime.getZone()),
              period).calculate();
            console.println(baseItem + "." + pastPeriod + " span from " + fromTime + " to " + toTime);
            earliest = incrementer.apply(earliest);

            HistoricItem start = get(persistence, createFilterCriteria(baseItem, fromTime, true));
            HistoricItem end = get(persistence, createFilterCriteria(baseItem, toTime, false));

            if (start == null || start.getState() == null) {
                continue;
            }
            if (end == null || end.getState() == null) {
                continue;
            }

            State calculation = calculate(start.getState(), end.getState());
            console.println(baseItem + "." + pastPeriod + " stored data " + start.getState() + ", " + end.getState() + " result " + calculation);
            if (calculation == null) {
                continue;
            }

            persistence.store(item, Date.from(toTime.toInstant()), calculation);
            pastPeriod++;
          } while (now.isAfter(earliest.toInstant()));
          console.println("");
        } else {
            console.println("No data found for " + baseItem);
        }
      } else {
          console.println("Persistence service is not queryable");
      }
    } else {
        console.println("Item " + item.getName() + " does not have a base" + baseItem);
    }
  }

  public State calculate(State previous, State current) {
    if (current instanceof QuantityType) {
      QuantityType to = (QuantityType<?>) current;
      if (previous instanceof QuantityType) {
        QuantityType from = (QuantityType<?>) previous;
        return to.subtract(from);
      }
    }

    if (current instanceof DecimalType) {
      DecimalType to = (DecimalType) current;
      if (previous instanceof DecimalType) {
        DecimalType from = (DecimalType) previous;
        return new DecimalType(to.toBigDecimal().subtract(from.toBigDecimal()));
      }
    }

    return null;
  }

  public HistoricItem get(QueryablePersistenceService service, FilterCriteria filter) {
    Iterable<HistoricItem> result = service.query(filter);

    if (result.iterator().hasNext()) {
      return result.iterator().next();
    }

    return null;
  }

  private FilterCriteria createFilterCriteria(String item, ZonedDateTime timestamp, boolean start) {
    if (timestamp == null) {
        return null;
    }

    FilterCriteria filter = new FilterCriteria() {
      @Override
      public String toString() {
        return "FilterCriteria [begin=" + getBeginDate() + ", end=" + getEndDate() + ", operator=" + getOperator() + ", ordering=" + getOrdering() + "]";
      }
    };

    if (start) {
      filter.setBeginDate(timestamp).setOrdering(Ordering.ASCENDING).setOperator(Operator.GTE);
    } else {
      filter.setEndDate(timestamp).setOrdering(Ordering.DESCENDING).setOperator(Operator.LTE);
    }
    filter.setItemName(item);
    filter.setPageSize(1);
    return filter;
  }

}
