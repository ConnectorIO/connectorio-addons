package org.connectorio.addons.test.persistence;

import java.time.ZonedDateTime;
import org.openhab.core.types.State;

public class Entry implements Comparable<Entry> {
  final ZonedDateTime time;
  final String item;
  final State state;

  Entry(ZonedDateTime time, String item, State state) {
    this.time = time;
    this.item = item;
    this.state = state;
  }

  public static Entry entry(ZonedDateTime time, String item, State state) {
    return new Entry(time, item, state);
  }

  @Override
  public int compareTo(Entry o) {
    return time.compareTo(o.time);
  }

}
