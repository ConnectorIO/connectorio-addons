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
package org.connectorio.addons.compute.efficiency.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.connectorio.addons.compute.efficiency.internal.memo.StateCollector;
import org.connectorio.addons.compute.efficiency.internal.ventilation.heatex.HeatExConfig;
import org.connectorio.addons.compute.efficiency.internal.ventilation.heatex.HeatExState;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.config.core.status.ConfigStatusMessage.Builder;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ConfigStatusThingHandler;
import org.openhab.core.types.Command;

public class EfficiencyHandler extends ConfigStatusThingHandler {

  private final ItemRegistry itemRegistry;
  private final StateCollector<ItemStateChangedEvent> collector;
  private HeatExConfig config;
  private HeatExState heatEx;

  public EfficiencyHandler(Thing thing, ItemRegistry itemRegistry, StateCollector<ItemStateChangedEvent> collector) {
    super(thing);
    this.itemRegistry = itemRegistry;
    this.collector = collector;
  }

  @Override
  public void initialize() {
    this.config = getConfigAs(HeatExConfig.class);

    if (this.config != null) {
      updateStatus(ThingStatus.ONLINE);
    } else {
      updateStatus(ThingStatus.OFFLINE);
    }
  }

  @Override
  public Collection<ConfigStatusMessage> getConfigStatus() {
    HeatExConfig config = getConfigAs(HeatExConfig.class);

    List<ConfigStatusMessage> status = new ArrayList<>();
    if (config.extractTemperature == null) {
      status.add(Builder.pending("extractTemperature").build());
    } else {
      validate(status, "extractTemperature", config.extractTemperature);
    }

    if (config.supplyTemperature == null) {
      status.add(Builder.pending("supplyTemperature").build());
    } else {
      validate(status, "supplyTemperature", config.supplyTemperature);
    }

    if (config.intakeTemperature == null) {
      status.add(Builder.pending("intakeTemperature").build());
    } else {
      validate(status, "intakeTemperature", config.intakeTemperature);
    }

    return status;
  }

  private void validate(List<ConfigStatusMessage> status, String parameter, String itemName) {
    try {
      Item registryItem = itemRegistry.getItem(itemName);
      if (!registryItem.getType().startsWith(CoreItemFactory.NUMBER)) {
        status.add(ConfigStatusMessage.Builder.error(parameter).withMessageKeySuffix("wrong-type").build());
      }
    } catch (ItemNotFoundException e) {
      status.add(ConfigStatusMessage.Builder.error(parameter).withMessageKeySuffix("missing").build());
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    // things of this type are rather passive thus they can't do much ;-)

  }

  @Override
  public void channelLinked(ChannelUID channelUID) {
    super.channelLinked(channelUID);

    this.heatEx = new HeatExState(System::currentTimeMillis, getCallback(), channelUID, config);
    this.collector.addStateReceiver(heatEx);
  }

  @Override
  public void channelUnlinked(ChannelUID channelUID) {
    super.channelUnlinked(channelUID);

    this.collector.removeStateReceiver(heatEx);
    this.heatEx = null;
  }

}
