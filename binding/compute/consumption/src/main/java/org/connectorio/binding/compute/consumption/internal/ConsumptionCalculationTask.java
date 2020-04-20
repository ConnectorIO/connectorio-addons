/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.binding.compute.consumption.internal;

import java.time.ZonedDateTime;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;

public class ConsumptionCalculationTask implements Runnable {

  private final TimeZoneProvider timeZoneProvider;
  private final Item item;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;

  public ConsumptionCalculationTask(TimeZoneProvider timeZoneProvider, Item item, ThingHandlerCallback callback, ChannelUID channelUID) {
    this.timeZoneProvider = timeZoneProvider;
    this.item = item;
    this.callback = callback;
    this.channelUID = channelUID;
  }

  @Override
  public void run() {
    ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone()).minusSeconds(60);

    DecimalType deltaSince = PersistenceExtensions.deltaSince(item, now.toInstant());
    if (deltaSince != null) {
      callback.stateUpdated(channelUID, deltaSince);
    }
  }
}
