package org.connectorio.addons.communication.watchdog;

import java.time.Duration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Watchdog builder allows to specify settings for watchdog and channels which should be polled on
 * cyclic basis.
 *
 * All settings specified in builder will impact behavior of Watchdog itself.
 */
public interface WatchdogBuilder {

  WatchdogBuilder withChannel(ChannelUID channel, long timeoutPeriodMs);
  WatchdogBuilder withChannel(ChannelUID channel, Duration duration);

  WatchdogBuilder withTimeoutDelay(long timeout);

  WatchdogBuilder withTimeoutMultiplier(int multiplier);

  Watchdog build(ThingHandlerCallback callback, WatchdogListener listener);

}