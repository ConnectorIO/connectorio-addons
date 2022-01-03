package org.connectorio.addons.test.persistence;

import java.time.ZonedDateTime;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

class StubHistoricItem implements HistoricItem {

  private final Entry entry;

  StubHistoricItem(Entry entry) {
    this.entry = entry;
  }

  @Override
  public ZonedDateTime getTimestamp() {
    return entry.time;
  }

  @Override
  public State getState() {
    return entry.state;
  }

  @Override
  public String getName() {
    return entry.item;
  }

  public String toString() {
    return entry.time + ":" + entry.item + "=" + entry.state;
  }
}