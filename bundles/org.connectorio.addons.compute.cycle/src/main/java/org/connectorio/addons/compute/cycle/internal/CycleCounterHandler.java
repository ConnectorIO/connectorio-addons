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
package org.connectorio.addons.compute.cycle.internal;

import static org.connectorio.addons.compute.cycle.internal.CycleBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.connectorio.addons.compute.cycle.internal.config.CounterChannelConfig;
import org.connectorio.addons.compute.cycle.internal.config.DifferenceChannelConfig;
import org.connectorio.addons.compute.cycle.internal.operation.CycleCount;
import org.connectorio.addons.compute.cycle.internal.operation.CycleDifference;
import org.connectorio.addons.compute.cycle.internal.operation.CycleTime;
import org.connectorio.addons.compute.cycle.internal.config.CycleCounterConfig;
import org.connectorio.addons.compute.cycle.internal.memo.StateCollector;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage.Builder;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateChangedEvent;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;

public class CycleCounterHandler extends ConfigStatusThingHandler {

  private final ItemRegistry itemRegistry;
  private final StateCollector<ItemStateChangedEvent> collector;
  private CycleCounterConfig config;
  private TriggerReceiver receiver;

  public CycleCounterHandler(Thing thing, ItemRegistry itemRegistry, StateCollector<ItemStateChangedEvent> collector) {
    super(thing);
    this.itemRegistry = itemRegistry;
    this.collector = collector;
  }

  @Override
  public void initialize() {
    this.config = getConfigAs(CycleCounterConfig.class);

    if (this.config != null) {
      updateStatus(ThingStatus.ONLINE);
    } else {
      updateStatus(ThingStatus.OFFLINE);
    }

    this.receiver = new TriggerReceiver();
    collector.addStateReceiver(receiver);
  }

  @Override
  public void dispose() {
    if (receiver != null) {
      collector.removeStateReceiver(receiver);
    }
  }

  @Override
  public Collection<ConfigStatusMessage> getConfigStatus() {
    CycleCounterConfig config = getConfigAs(CycleCounterConfig.class);

    List<ConfigStatusMessage> status = new ArrayList<>();
    if (config.trigger == null) {
      status.add(Builder.pending("trigger").build());
    } else {
      validate(status, "trigger", config.trigger);
    }

    return status;
  }

  private void validate(List<ConfigStatusMessage> status, String parameter, String itemName) {
    try {
      Item registryItem = itemRegistry.getItem(itemName);
      if (!registryItem.getType().startsWith(CoreItemFactory.SWITCH) && !registryItem.getType().startsWith(CoreItemFactory.CONTACT)) {
        status.add(Builder.error(parameter).withMessageKeySuffix("wrong-type").build());
      }
    } catch (ItemNotFoundException e) {
      status.add(Builder.error(parameter).withMessageKeySuffix("missing").build());
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    // things of this type are rather passive thus they can't do much ;-)

  }

  @Override
  public void channelLinked(ChannelUID channelUID) {
    super.channelLinked(channelUID);

    Channel channel = getThing().getChannel(channelUID);

    if (CycleBindingConstants.TIME.equals(channelUID.getId())) {
      receiver.addOperation(new CycleTime(System::currentTimeMillis, getCallback(), channelUID, config));
    }

    if (CycleBindingConstants.COUNT.equals(channelUID.getId())) {
      CounterChannelConfig channelConfig = channel.getConfiguration().as(CounterChannelConfig.class);
      receiver.addOperation(new CycleCount(System::currentTimeMillis, getCallback(), channelUID, channelConfig));
    }

    ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
    if (DIFFERENCE_TYPE.equals(channelTypeUID)) {
      DifferenceChannelConfig channelConfig = channel.getConfiguration().as(DifferenceChannelConfig.class);
      receiver.addOperation(new CycleDifference(itemRegistry, getCallback(), channelUID, channelConfig));
    }
  }

  @Override
  public void channelUnlinked(ChannelUID channelUID) {
    super.channelUnlinked(channelUID);

    receiver.removeOperation(channelUID);
  }

}
