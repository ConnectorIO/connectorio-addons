package org.connectorio.addons.test.persistence;

import java.time.ZonedDateTime;
import java.util.Date;
import org.openhab.core.persistence.PersistenceItemInfo;

class StubItemInfo implements PersistenceItemInfo {

  final String item;
  final ZonedDateTime earliest;
  final ZonedDateTime latest;
  final int count;

  public StubItemInfo(String item, ZonedDateTime earliest, ZonedDateTime latest) {
    this(item, earliest, latest, 1);
  }

  public StubItemInfo(String item, ZonedDateTime earliest, ZonedDateTime latest, int count) {
    this.item = item;
    this.earliest = earliest;
    this.latest = latest;
    this.count = count;
  }

  @Override
  public String getName() {
    return item;
  }

  @Override
  public Integer getCount() {
    return count;
  }

  @Override
  public Date getEarliest() {
    return new Date(earliest.toInstant().toEpochMilli());
  }

  @Override
  public Date getLatest() {
    return new Date(latest.toInstant().toEpochMilli());
  }

}