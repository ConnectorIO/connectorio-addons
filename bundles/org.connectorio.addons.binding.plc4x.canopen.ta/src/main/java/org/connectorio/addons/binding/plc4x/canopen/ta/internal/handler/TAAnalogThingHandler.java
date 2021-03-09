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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import java.util.Optional;
import javax.measure.Dimension;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogChannelConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.OldAnalogChannelConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.ValueCallback;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.AnalogValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.quantity.Quantities;

public class TAAnalogThingHandler extends BaseThingHandler implements ValueCallback<Value<?>> {

  private final Logger logger = LoggerFactory.getLogger(TAAnalogThingHandler.class);
  private AnalogObjectConfig config;
  private TADevice device;

  private AnalogUnit unit;

  public TAAnalogThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    TADeviceThingHandler handler = Optional.ofNullable(getBridge()).map(Bridge::getHandler)
      .filter(TADeviceThingHandler.class::isInstance)
      .map(TADeviceThingHandler.class::cast)
      .orElse(null);

    this.config = getConfigAs(AnalogObjectConfig.class);

    if (handler == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
      return;
    }

    handler.getDevice().whenComplete((result, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, error != null ? error.getMessage() : "Unknown error");
        return;
      }

      Channel channel = getThing().getChannel(getThing().getThingTypeUID().getId());
      AnalogChannelConfig channelConfig = channel.getConfiguration().as(AnalogChannelConfig.class);
      if (channelConfig != null && channelConfig.unit != null) {
        unit = channelConfig.unit;
      }

      if (config.readObjectIndex != 0) {
        result.addValueCallback(this);
        result.addAnalogOutput(config.readObjectIndex, unit);
      }
      if (config.writeObjectIndex != 0) {
        result.addValueCallback(this);
        result.addAnalogInput(config.writeObjectIndex, unit);
      }

      this.device = result;
      updateStatus(ThingStatus.ONLINE);
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (config.writeObjectIndex == 0) {
      logger.warn("Ignoring write request {} to channel {} cause write object index is not set", command, channelUID);
      return;
    }

    Value<?> value = createValue(command);
    if (value == null) {
      logger.warn("Failed to convert value {} for channel {} to desired unit {} skipping update.", command, channelUID, unit);
      return;
    }
    device.write(config.writeObjectIndex, value);
  }

  private Value<?> createValue(Command command) {
    if (command instanceof QuantityType) {
      QuantityType quantity = (QuantityType<?>) command;

      UnitConverter converter = quantity.getUnit().getConverterTo(unit.getUnit());
      if (converter != null) {
        double convert = converter.convert(quantity.doubleValue());
        return new AnalogValue(Quantities.getQuantity(convert, unit.getUnit()), unit);
      }
    }

    if (command instanceof Number) {
      return new AnalogValue(Quantities.getQuantity((Number) command, unit.getUnit()), unit);
    }

    logger.warn("Unsupported value type {}", command.getClass());
    return null;
  }

  @Override
  public void accept(int index, Value<?> value) {
    if (config.readObjectIndex == index && value instanceof AnalogValue) {
      AnalogValue analog = (AnalogValue) value;
      logger.debug("Received update of matching analog object {} with value {}", index, analog);

      ThingHandlerCallback callback = getCallback();
      if (callback != null) {
        Thing thing = getThing();
        Channel channel = thing.getChannel(thing.getThingTypeUID().getId());
        if (channel != null) {
          callback.stateUpdated(channel.getUID(), createState(analog));
        }
      } else {
        logger.warn("Ignoring state update {} for {}, handler not ready", analog, config.readObjectIndex);
      }
    }
  }

  protected State createState(AnalogValue value) {
    Quantity<?> quantity = value.getValue();
    return QuantityType.valueOf(quantity.getValue().doubleValue(), quantity.getUnit());
  }

}
