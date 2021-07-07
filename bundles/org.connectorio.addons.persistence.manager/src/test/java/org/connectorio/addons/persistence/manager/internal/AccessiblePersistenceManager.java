package org.connectorio.addons.persistence.manager.internal;

import org.openhab.core.common.SafeCaller;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.internal.PersistenceManagerImpl;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.service.ReadyService;

public class AccessiblePersistenceManager extends PersistenceManagerImpl {

  public AccessiblePersistenceManager(CronScheduler scheduler, ItemRegistry itemRegistry, SafeCaller safeCaller, ReadyService readyService) {
    super(scheduler, itemRegistry, safeCaller, readyService);
  }

  @Override
  public void activate() {
    super.activate();
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

  @Override
  public void addPersistenceService(PersistenceService persistenceService) {
    super.addPersistenceService(persistenceService);
  }

  @Override
  public void removePersistenceService(PersistenceService persistenceService) {
    super.removePersistenceService(persistenceService);
  }
}
