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

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryPersistenceService implements ModifiablePersistenceService {

  public final static String ID = "memory";

  private final Logger logger = LoggerFactory.getLogger(ModifiablePersistenceService.class);
  private final Map<String, MemoryBucket> buckets = new ConcurrentHashMap<>();

  private final TimeZoneProvider timeZoneProvider;

  // number of entries for each bucket
  private int limit = 100;
  private final String id;
  private String label = "Memory persistence service";

  public MemoryPersistenceService(TimeZoneProvider timeZoneProvider) {
    this(ID, timeZoneProvider);
  }

  public MemoryPersistenceService(String id, TimeZoneProvider timeZoneProvider) {
    this.id = id;
    this.timeZoneProvider = timeZoneProvider;
  }

  @Modified
  public void update(Map<String, Object> config) {
    if (config.containsKey("limit")) {
      try {
        int newLimit = Integer.parseInt("" + config.get("limit"));
        this.limit = newLimit > 0 ? newLimit : 100;

        for (MemoryBucket bucket : buckets.values()) {
          bucket.truncate(limit);
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid value for limit");
      }
    }

    if (config.containsKey("label")) {
      this.label = "" + config.get("label");
    } else {
      this.label = "Memory Persistence Service (" + this.id + ")";
    }
  }

  @Override
  public Iterable<HistoricItem> query(FilterCriteria criteria) {
    logger.debug("Received call for item {} and date range {}-{}", criteria.getItemName(), criteria.getBeginDate(), criteria.getEndDate());
    if (criteria.getItemName() == null || !buckets.containsKey(criteria.getItemName())) {
      logger.debug("Received call can not be handled - unknown bucket or missing data");
      return Collections.emptyList();
    }

    logger.debug("Querying item {} with date range {}-{}", criteria.getItemName(), criteria.getBeginDate(), criteria.getEndDate());
    return buckets.get(criteria.getItemName()).process(bucket -> {
      Stream<Entry<ZonedDateTime, State>> entries = filtering(bucket.entries(), criteria);
      entries = sorting(entries, criteria);
      entries = paging(entries, criteria);

      return entries.map(entry -> toHistoricItem(criteria.getItemName(), entry))
        .collect(Collectors.toList());
    });
  }

  @Override
  public Set<PersistenceItemInfo> getItemInfo() {
    Set<PersistenceItemInfo> infos = new TreeSet<>(Comparator.comparing(PersistenceItemInfo::getName));
    for (Entry<String, MemoryBucket> entry : buckets.entrySet()) {
      MemoryBucketInfo info = entry.getValue().process(bucket -> new MemoryBucketInfo(entry.getKey(), bucket.getSize(), bucket.getEarliest(), bucket.getOldest()));
      infos.add(info);
    }
    return infos;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getLabel(Locale locale) {
    return label;
  }

  @Override
  public void store(Item item) {
    ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());

    memorize(item.getName(), now, item.getState());
  }

  @Override
  public void store(Item item, String alias) {
    ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());

    String name = alias != null ? alias : item.getName();
    memorize(name, now, item.getState());
  }

  @Override
  public void store(Item item, Date date, State state) {
    ZonedDateTime time = ZonedDateTime.ofInstant(date.toInstant(), timeZoneProvider.getTimeZone());
    memorize(item.getName(), time, state);
  }

  @Override
  public boolean remove(FilterCriteria criteria) throws IllegalArgumentException {
    if (criteria.getItemName() == null || !buckets.containsKey(criteria.getItemName())) {
      return false;
    }

    logger.trace("Removing entries from bucket {}", criteria.getItemName());
    return buckets.get(criteria.getItemName()).process(bucket -> {
      Stream<Entry<ZonedDateTime, State>> entries = filtering(bucket.entries(), criteria);

      entries = sorting(entries, criteria);
      entries = paging(entries, criteria);
      entries.forEach(bucket::remove);
      return true;
    });
  }

  @Override
  public List<PersistenceStrategy> getDefaultStrategies() {
    return Collections.emptyList();
  }

  private void memorize(String name, ZonedDateTime time, State state) {
    if (state instanceof UnDefType) {
      return;
    }

    MemoryBucket bucket = buckets.computeIfAbsent(name, key -> new MemoryBucket(limit));
    logger.trace("Storing entry {}={} in bucket {} for item {}", time, state, bucket, name);
    bucket.append(time, state);
  }
  private HistoricItem toHistoricItem(String name, Entry<ZonedDateTime, State> entry) {
    return new MemoryHistoricItem(name, entry.getKey(), entry.getValue());
  }

  private static Stream<Entry<ZonedDateTime, State>> filtering(Stream<Entry<ZonedDateTime, State>> entries, FilterCriteria criteria) {
    return entries.filter(entry -> evaluate(entry, criteria));
  }

  private static Stream<Entry<ZonedDateTime, State>> sorting(Stream<Entry<ZonedDateTime, State>> entries, FilterCriteria criteria) {
    if (criteria.getOrdering() == Ordering.DESCENDING) {
      Comparator<Entry<ZonedDateTime, State>> comparator = Entry.comparingByKey();
      entries = entries.sorted(comparator.reversed());
    }
    return entries;
  }

  private static Stream<Entry<ZonedDateTime, State>> paging(Stream<Entry<ZonedDateTime, State>> stream, FilterCriteria criteria) {
    int offset = criteria.getPageNumber() * criteria.getPageSize();
    return stream.skip(offset)
      .limit(criteria.getPageSize());
  }

  private static boolean evaluate(Entry<ZonedDateTime, State> entry, FilterCriteria criteria) {
    ZonedDateTime beginDate = criteria.getBeginDate();
    if (beginDate != null && !(entry.getKey().isAfter(beginDate) || beginDate.isEqual(entry.getKey()))) {
      return false;
    }

    ZonedDateTime endDate = criteria.getEndDate();
    if (endDate != null && !(entry.getKey().isBefore(endDate) || endDate.isEqual(entry.getKey()))) {
      return false;
    }

    return true;
  }


}
