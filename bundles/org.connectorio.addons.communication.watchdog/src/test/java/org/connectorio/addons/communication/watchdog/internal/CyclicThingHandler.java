/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.communication.watchdog.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.communication.watchdog.Watchdog;
import org.connectorio.addons.communication.watchdog.contrib.ThingStatusWatchdogListener;
import org.connectorio.addons.communication.watchdog.WatchdogManager;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;

// This class does not use base class from openHAB lib due to "non-null" annotation processing
// troubles. Beyond that it is ready for reuse.
public abstract class CyclicThingHandler implements ThingHandler {

  private final ScheduledExecutorService scheduler = null;

  private WatchdogManager watchdogManager;
  private Watchdog watchdog;

  // tag::injection[]
  @SuppressWarnings("all")
  public CyclicThingHandler(Thing thing, WatchdogManager watchdogManager) {
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

  protected abstract ThingHandlerCallback getCallback();
}
