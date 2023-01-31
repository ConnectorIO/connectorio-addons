package org.connectorio.addons.communication.watchdog;

import java.util.Map;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

public interface Watchdog {

  //
  boolean isTimedOut();

  ThingHandlerCallback getCallbackWrapper();

  long getTimeoutEventDelay();

  Map<WatchdogCondition, Long> getConditionIntervalMap();

  void mark(ChannelUID channelUID);

  void close();
}
