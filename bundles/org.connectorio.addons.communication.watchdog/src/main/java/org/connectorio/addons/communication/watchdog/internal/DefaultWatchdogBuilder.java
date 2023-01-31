package org.connectorio.addons.communication.watchdog.internal;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.connectorio.addons.communication.watchdog.Watchdog;
import org.connectorio.addons.communication.watchdog.WatchdogBuilder;
import org.connectorio.addons.communication.watchdog.WatchdogClock;
import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.connectorio.addons.communication.watchdog.WatchdogListener;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

public class DefaultWatchdogBuilder implements WatchdogBuilder {

  private WatchdogClock clock;
  private DefaultWatchdogManager manager;
  private final Thing thing;
  private final Map<ChannelUID, WatchdogCondition> conditions = new HashMap<>();
  private long delayMs;
  private int multiplier;

  public DefaultWatchdogBuilder(WatchdogClock clock, DefaultWatchdogManager manager, Thing thing) {
    this.clock = clock;
    this.manager = manager;
    this.thing = thing;
  }

  @Override
  public WatchdogBuilder withChannel(ChannelUID channel, long timeoutPeriodMs) {
    conditions.put(channel, new TimeoutCondition(clock, channel, timeoutPeriodMs));
    return this;
  }

  @Override
  public WatchdogBuilder withChannel(ChannelUID channel, Duration duration) {
    conditions.put(channel, new TimeoutCondition(clock, channel, duration.toMillis()));
    return this;
  }

  @Override
  public WatchdogBuilder withTimeoutDelay(long delayMs) {
    this.delayMs = delayMs;
    return this;
  }

  @Override
  public WatchdogBuilder withTimeoutMultiplier(int multiplier) {
    this.multiplier = multiplier;
    return this;
  }

  @Override
  public Watchdog build(ThingHandlerCallback callback, WatchdogListener listener) {
    DefaultWatchdog watchdog = new DefaultWatchdog(callback, conditions, manager::close);
    manager.registerWatchdog(thing, watchdog, listener);
    return watchdog;
  }

}
