package org.connectorio.addons.communication.watchdog.internal;

import java.time.Clock;
import org.connectorio.addons.communication.watchdog.WatchdogClock;

public class DefaultWatchdogClock implements WatchdogClock {

  private final Clock clock;

  public DefaultWatchdogClock() {
    this(Clock.systemUTC());
  }

  public DefaultWatchdogClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public long getTimestamp() {
    return clock.millis();
  }

}
