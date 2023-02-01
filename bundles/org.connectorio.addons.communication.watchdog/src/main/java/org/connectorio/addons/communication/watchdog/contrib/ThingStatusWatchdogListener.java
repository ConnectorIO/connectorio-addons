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
package org.connectorio.addons.communication.watchdog.contrib;

import java.util.HashSet;
import java.util.Set;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogInitializedEvent;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogRecoveryEvent;
import org.connectorio.addons.communication.watchdog.WatchdogEvent.WatchdogTimeoutEvent;
import org.connectorio.addons.communication.watchdog.WatchdogListener;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Universal {@link WatchdogListener} which updated associated thing status to online/offline.
 *
 * Additional functionality is inclusion of failed channels into status details.
 */
public class ThingStatusWatchdogListener implements WatchdogListener {

  private final Set<ChannelUID> channels = new HashSet<>();
  private final Thing thing;
  private final ThingHandlerCallback callback;
  private final int limit;

  public ThingStatusWatchdogListener(Thing thing, ThingHandlerCallback callback) {
    this(thing, callback, 1);
  }

  public ThingStatusWatchdogListener(Thing thing, ThingHandlerCallback callback, int limit) {
    this.thing = thing;
    this.callback = callback;
    this.limit = limit;
  }

  @Override
  public void timeout(WatchdogTimeoutEvent event) {
    channels.add(event.getChannel());
    if (channels.size() >= limit) {
      String description = createDescription(channels);
      ThingStatusInfo info = new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, description);
      callback.statusUpdated(thing, info);
    }
  }

  @Override
  public void initialized(WatchdogInitializedEvent event) {
    channels.clear();

    ThingStatusInfo info = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.ONLINE.CONFIGURATION_PENDING, "Awaiting for communication");
    callback.statusUpdated(thing, info);
  }

  @Override
  public void recovery(WatchdogRecoveryEvent event) {
    channels.remove(event.getChannel());
    if (channels.size() < limit) {
      callback.statusUpdated(thing, new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }
  }

  private static String createDescription(Set<ChannelUID> channels) {
    if (channels.size() > 1) {
      String channelStr = channels.stream()
          .map(ChannelUID::getId)
          .reduce("", (left, right) -> left + ", " + right);
      return "Multiple channels did not receive update in specified time " + channelStr;
    }
    return "Channel " + channels.iterator().next().getId() + " did not receive update in specified time";
  }

}
