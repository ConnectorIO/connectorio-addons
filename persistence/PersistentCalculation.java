package org.connectorio.persistence.api;

import java.time.Instant;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.persistence.HistoricItem;

public interface PersistentCalculation {

  Boolean changedSince(Item item, Instant timestamp);
  Boolean updatedSince(Item item, Instant timestamp);

  DecimalType deltaSince(Item item, Instant timestamp);
  DecimalType evolutionRate(Item item, Instant timestamp);
  DecimalType averageSince(Item item, Instant timestamp);
  DecimalType sumSince(Item item, Instant timestamp);
  Instant lastUpdate(Item item);


  Iterable<HistoricItem> getAllStatesSince(Item item, Instant timestamp);

  HistoricItem maximumSince(Item item, Instant timestamp);
  HistoricItem minimumSince(Item item, Instant timestamp);
  HistoricItem previousState(Item item);
  HistoricItem previousState(Item item, boolean skipEqual);

}
