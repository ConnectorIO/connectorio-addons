package org.connectorio.addons.communication.watchdog;

import org.openhab.core.thing.Thing;

public interface WatchdogManager {

  WatchdogBuilder builder(Thing thing);

}