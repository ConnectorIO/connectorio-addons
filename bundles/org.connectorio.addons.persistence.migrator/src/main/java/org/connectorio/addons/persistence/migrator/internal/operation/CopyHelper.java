package org.connectorio.addons.persistence.migrator.internal.operation;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.persistence.migrator.internal.item.StubItem;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CopyHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(CopyHelper.class);

  public static void copy(ModifiablePersistenceService sourceService, Item sourceItem, ModifiablePersistenceService targetService, Item targetItem) {
    int page = 0;
    int pageSize = 1000;
    int amount = 0;
    boolean nextPage = false;
    long startTime = System.currentTimeMillis();

    LOGGER.info("Copy data from {} to {}", sourceItem.getName(), targetItem.getName());

    do {
      LOGGER.debug("Copy page {} from {} to {}", page, sourceItem.getName(), targetItem.getName());
      for (HistoricItem historic : sourceService.query(new FilterCriteria().setPageSize(pageSize).setPageNumber(page)
          .setOrdering(Ordering.ASCENDING).setItemName(sourceItem.getName()))) {
        State state = historic.getState();
        if (state == UnDefType.UNDEF || state == UnDefType.NULL) {
          continue;
        }
        ZonedDateTime timestamp = historic.getTimestamp();
        targetService.store(new StubItem(targetItem, state), Date.from(timestamp.toInstant()), state);
        amount++;
        if (amount == pageSize) {
          page++;
          nextPage = true;
          amount = 0;
        } else {
          nextPage = false;
        }
      }
    } while (nextPage);

    long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
    LOGGER.info("Copied {} pages containing {} data entries in {} seconds", page, (page == 0 ? "up to " + pageSize : (pageSize * page) + amount), seconds);
  }

}
