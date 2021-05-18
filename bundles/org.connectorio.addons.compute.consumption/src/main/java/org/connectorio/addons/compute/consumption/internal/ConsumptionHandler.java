/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.compute.consumption.internal;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

public class ConsumptionHandler extends BaseThingHandler {

  private final TimeZoneProvider timeZoneProvider;
  private final ItemRegistry itemRegistry;
  private Item item;

  public ConsumptionHandler(Thing thing, TimeZoneProvider timeZoneProvider,
    ItemRegistry itemRegistry) {
    super(thing);
    this.timeZoneProvider = timeZoneProvider;
    this.itemRegistry = itemRegistry;
  }

  @Override
  public void initialize() {
    String item = getConfigAs(ConsumptionConfig.class).item;

    if (item == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, "Missing configuration, please set item");
    } else {
      try {
        Item registryItem = itemRegistry.getItem(item);
        if (registryItem.getType().startsWith(CoreItemFactory.NUMBER)) {
          this.item = registryItem;
          updateStatus(ThingStatus.ONLINE);
        } else {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configured item is not a number");
        }
      } catch (ItemNotFoundException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Item not found");
      }
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  public void channelLinked(ChannelUID channelUID) {
    super.channelLinked(channelUID);

    LocalTime now = LocalTime.now();
    long initialDelay = now.getSecond() == 0 ? 60 : 60 - now.getSecond();

    String computationKind = getChannelId(channelUID);
    switch (computationKind) {
      case ConsumptionBindingConstants.ONE_MINUTE:
        scheduler.scheduleAtFixedRate(new ConsumptionCalculationTask(timeZoneProvider, item, getCallback(), channelUID), initialDelay, 60, TimeUnit.SECONDS);
        break;
    }
  }

  private String getChannelId(ChannelUID channelUID) {
    return channelUID.getId();
  }

  @Override
  public void channelUnlinked(ChannelUID channelUID) {
    super.channelUnlinked(channelUID);
  }

}
