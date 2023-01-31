package org.connectorio.addons.communication.watchdog.internal;

import java.util.concurrent.atomic.AtomicReference;
import org.connectorio.addons.communication.watchdog.WatchdogClock;
import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutCondition implements WatchdogCondition {

  private final Logger logger = LoggerFactory.getLogger(TimeoutCondition.class);
  private final AtomicReference<State> state = new AtomicReference<>();

  private final WatchdogClock clock;
  private final long timeoutPeriodMs;
  private final ChannelUID channel;

  private Long lastUpdate;

  public TimeoutCondition(WatchdogClock clock, ChannelUID channel, long timeoutPeriodMs) {
    this.clock = clock;
    this.timeoutPeriodMs = timeoutPeriodMs;
    this.channel = channel;
  }

  @Override
  public State getState() {
    return state.get();
  }

  @Override
  public State evaluate() {
    Long updateWindow = clock.getTimestamp() - timeoutPeriodMs;
    if (this.lastUpdate == null) {
      // assume we haven't seen anything for last window
      this.lastUpdate = updateWindow;
      return State.INITIALIZATION;
    }
    logger.debug("Last update time {} should be higher than {}", lastUpdate, updateWindow);
    State currentState = lastUpdate > updateWindow ? State.OK : State.FAILURE;
    state.set(currentState);
    return currentState;
  }

  @Override
  public long getInterval() {
    return timeoutPeriodMs;
  }

  @Override
  public void mark() {
    this.lastUpdate = clock.getTimestamp();
    logger.debug("Channel {} received update at {}.", channel, lastUpdate);
  }

  @Override
  public ChannelUID getChannel() {
    return channel;
  }

  @Override
  public String toString() {
    return "TimeoutCondition [" + channel + ", timeout=" + timeoutPeriodMs + "ms]";
  }
}
