package org.connectorio.addons.test.persistence;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;

public class StubPersistenceService implements PersistenceService {

  private final String id;
  protected final List<Entry> entries = new ArrayList<>();

  protected StubPersistenceService(String id, Entry ... entries) {
    append(Arrays.asList(entries));
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getLabel(Locale locale) {
    return id;
  }

  @Override
  public void store(Item item) {
    store(item, null);
  }

  @Override
  public void store(Item item, String alias) {
    entries.add(new Entry(ZonedDateTime.now(), item.getName(), item.getState()));
  }

  public static List<Entry> getEntries(PersistenceService service) {
    if (service instanceof StubPersistenceService) {
      StubPersistenceService stub = (StubPersistenceService) service;
      return Collections.unmodifiableList(stub.entries);
    }
    return Collections.emptyList();
  }

  public static void updateEntries(PersistenceService service, Entry ... entries) {
    if (service instanceof StubPersistenceService) {
      StubPersistenceService stub = (StubPersistenceService) service;
      stub.append(Arrays.asList(entries));
    }
  }

  @Override
  public List<PersistenceStrategy> getDefaultStrategies() {
    return Collections.emptyList();
  }

  private void append(List<Entry> entries) {
    this.entries.addAll(entries);
    Collections.sort(this.entries, Comparator.comparing(entry -> entry.time));
  }

  public static PersistenceService service(String id) {
    return new StubPersistenceService(id);
  }

  public static QueryablePersistenceService queryable(String id, Entry ... entries) {
    return new QueryableStubPersistenceService(id, entries);
  }

  public static ModifiablePersistenceService modifiable(String id, Entry ... entries) {
    return null;
  }

}
