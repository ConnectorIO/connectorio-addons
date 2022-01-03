package org.connectorio.addons.test.persistence;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;

public class StubPersistenceRegistry implements PersistenceServiceRegistry {

  private final PersistenceService defaultService;
  private final Map<String, PersistenceService> services;

  public StubPersistenceRegistry(PersistenceService defaultService, PersistenceService ... services) {
    this(defaultService, Arrays.stream(services).collect(Collectors.toMap(
      PersistenceService::getId, Function.identity()
    )));
  }

  public StubPersistenceRegistry(PersistenceService defaultService, Map<String, PersistenceService> serviceMap) {
    this.defaultService = defaultService;
    this.services = serviceMap;
  }

  @Override
  public PersistenceService getDefault() {
    return defaultService;
  }

  @Override
  public PersistenceService get(String serviceId) {
    if (defaultService.getId().equals(serviceId)) {
      return defaultService;
    }
    return services.get(serviceId);
  }

  @Override
  public String getDefaultId() {
    return defaultService.getId();
  }

  @Override
  public Set<PersistenceService> getAll() {
    Set<PersistenceService> services = new LinkedHashSet<>(this.services.values());
    services.add(defaultService);
    return Collections.unmodifiableSet(services);
  }
}
