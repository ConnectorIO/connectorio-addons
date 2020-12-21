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
package org.connectorio.addons.compute.cycle.internal.operation;

import java.math.BigDecimal;
import org.connectorio.addons.compute.cycle.internal.CycleOperation;
import org.connectorio.addons.compute.cycle.internal.config.DifferenceChannelConfig;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleDifference implements CycleOperation {

  private final Logger logger = LoggerFactory.getLogger(CycleDifference.class);
  private final ItemRegistry registry;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;
  private final DifferenceChannelConfig config;

  private State initial;

  public CycleDifference(ItemRegistry registry, ThingHandlerCallback callback, ChannelUID channelUID, DifferenceChannelConfig config) {
    this.registry = registry;
    this.callback = callback;
    this.channelUID = channelUID;
    this.config = config;
  }

  @Override
  public void open() {
    this.initial = readItemState();
  }

  private State readItemState() {
    try {
      Item item = registry.getItem(config.measure);
      State state = item.getStateAs(QuantityType.class);
      if (state != null) {
        return state;
      }
      DecimalType decimalType = item.getStateAs(DecimalType.class);
      if (decimalType != null) {
        return decimalType;
      }
      return DecimalType.ZERO;
    } catch (ItemNotFoundException e) {
      logger.debug("Could not find item {}", config.measure);
    }
    return null;
  }

  @Override
  public void close() {
    State lastValue = readItemState();

    if (initial != null && lastValue != null) {
      State value = null;
      if (lastValue instanceof QuantityType && initial instanceof QuantityType) {
        value = ((QuantityType) lastValue).subtract((QuantityType) initial);
      } else if (lastValue instanceof QuantityType && initial instanceof DecimalType) {
        QuantityType<?> quantity = (QuantityType) lastValue;
        value = new QuantityType<>(quantity.toBigDecimal().subtract(((DecimalType) initial).toBigDecimal()), quantity.getUnit());
      } else if (lastValue instanceof DecimalType && initial instanceof DecimalType) {
        // two decimals
        BigDecimal difference = ((DecimalType) lastValue).toBigDecimal().subtract(((DecimalType) initial).toBigDecimal());
        value = new DecimalType(difference);
      }
      if (value != null) {
        callback.stateUpdated(channelUID, value);
      } else {
        logger.info("Could not determine difference between initial value {} and last value {}", initial, lastValue);
      }
    }
  }

  @Override
  public ChannelUID getChannelId() {
    return channelUID;
  }

}
