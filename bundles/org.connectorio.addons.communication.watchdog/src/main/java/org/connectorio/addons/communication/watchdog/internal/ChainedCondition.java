package org.connectorio.addons.communication.watchdog.internal;

import org.connectorio.addons.communication.watchdog.WatchdogCondition;
import org.openhab.core.thing.ChannelUID;

public class ChainedCondition implements WatchdogCondition {

  private final WatchdogCondition condition1;
  private final WatchdogCondition condition2;

  public ChainedCondition(WatchdogCondition condition1, WatchdogCondition condition2) {
    this.condition1 = condition1;
    this.condition2 = condition2;
  }

  @Override
  public State evaluate() {
    try {
      return condition1.evaluate();
    } finally {
      condition2.evaluate();
    }
  }

  @Override
  public State getState() {
    return condition1.getState();
  }

  @Override
  public long getInterval() {
    return condition1.getInterval();
  }

  @Override
  public void mark() {
    condition1.mark();
    condition2.mark();
  }

  @Override
  public ChannelUID getChannel() {
    return condition1.getChannel();
  }
}
