package org.connectorio.addons.communication.watchdog;

import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogInitializedEvent;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogRecoveryEvent;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogTimeoutEvent;

public interface WatchdogListener {

  void timeout(WatchdogTimeoutEvent event);

  void initialized(WatchdogInitializedEvent event);

  void recovery(WatchdogRecoveryEvent event);

}
