package org.connectorio.addons.communication.watchdog.internal;

import java.util.concurrent.TimeUnit;
import org.connectorio.addons.communication.watchdog.Watchdog;
import org.connectorio.addons.communication.watchdog.contrib.ThingStatusWatchdogListener;
import org.connectorio.addons.communication.watchdog.WatchdogManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

public class CyclicThingHandler extends BaseThingHandler {

  private WatchdogManager watchdogManager;
  private Watchdog watchdog;

  // tag::injection[]
  public CyclicThingHandler(Thing thing, WatchdogManager watchdogManager) {
    super(thing);
    this.watchdogManager = watchdogManager;
  }
  // end::injection[]


  // tag::initialization[]
  @Override
  public void initialize() {
    ChannelUID channelUID = new ChannelUID(getThing().getUID(), "test");
    this.watchdog = watchdogManager.builder(getThing())
      .withChannel(channelUID, 1000) // <1>
      .build( // <2>
        getCallback(), // <3>
        new ThingStatusWatchdogListener(getThing(), getCallback()) // <4>
      );
    scheduler.scheduleAtFixedRate(() -> {
      // simulate cyclic update
      watchdog.getCallbackWrapper().stateUpdated(channelUID, OnOffType.ON); // <5>
    }, 0, 1000, TimeUnit.MILLISECONDS);
  }
  // end::initialization[]

  // tag::close[]
  @Override
  public void dispose() {
    if (watchdog != null) {
      watchdog.close();
    }
  }
  // end::close[]

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }
}
