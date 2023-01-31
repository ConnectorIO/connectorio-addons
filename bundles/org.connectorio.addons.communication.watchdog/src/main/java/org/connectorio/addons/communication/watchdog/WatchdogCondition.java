package org.connectorio.addons.communication.watchdog;

import org.openhab.core.thing.ChannelUID;

public interface WatchdogCondition {

  enum State {
    INITIALIZATION,
    FAILURE,
    OK
  }

  State evaluate();

  State getState();

  long getInterval();

  void mark();

  ChannelUID getChannel();
}
