package org.connectorio.addons.test.persistence;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.QueryablePersistenceService;

class QueryableStubPersistenceService extends StubPersistenceService
  implements QueryablePersistenceService {

  protected QueryableStubPersistenceService(String id, Entry... entries) {
    super(id, entries);
  }

  @Override
  public Iterable<HistoricItem> query(FilterCriteria filter) {
    int limit = filter.getPageSize();
    int start = limit * filter.getPageNumber();
    int end = start + limit;
    List<HistoricItem> list = new ArrayList<>();
    Predicate<Entry> predicate = predicates(filter);
    int matched = 0;
    for (Entry entry : entries) {
      if (predicate.test(entry)) {
        matched++;
        if (matched > end) {
          break;
        }

        if (matched > start) {
          list.add(new StubHistoricItem(entry));
        }
      }
    }
    return list;
  }

  private Predicate<Entry> predicates(FilterCriteria filter) {
    Predicate<Entry> predicate = (entry -> true);
    if (filter.getItemName() != null) {
      predicate = predicate.and(entry -> filter.getItemName().equals(entry.item));
    }
    if (filter.getBeginDate() != null) {
      predicate = predicate.and(entry -> entry.time.isAfter(filter.getBeginDate()));
    }
    if (filter.getEndDate() != null) {
      predicate = predicate.and(entry -> entry.time.isBefore(filter.getEndDate()));
    }

    return predicate;
  }

  @Override
  public Set<PersistenceItemInfo> getItemInfo() {
    Map<String, StubItemInfo> itemMap = new LinkedHashMap<>();

    for (Entry entry : entries) {
      if (!itemMap.containsKey(entry.item)) {
        itemMap.put(entry.item, new StubItemInfo(entry.item, entry.time, entry.time));
      } else {
        StubItemInfo itemInfo = itemMap.get(entry.item);
        if (itemInfo.earliest.isAfter(entry.time)) {
          itemMap.put(entry.item, earlier(itemInfo, entry.time));
        }
        if (itemInfo.latest.isAfter(entry.time)) {
          itemMap.put(entry.item, later(itemInfo, entry.time));
        }
      }
    }

    return new LinkedHashSet<>(itemMap.values());
  }

  private StubItemInfo earlier(StubItemInfo info, ZonedDateTime time) {
    return new StubItemInfo(info.item, time, info.latest, info.getCount() + 1);
  }

  private StubItemInfo later(StubItemInfo info, ZonedDateTime time) {
    return new StubItemInfo(info.item, info.earliest, time, info.getCount() + 1);
  }

}