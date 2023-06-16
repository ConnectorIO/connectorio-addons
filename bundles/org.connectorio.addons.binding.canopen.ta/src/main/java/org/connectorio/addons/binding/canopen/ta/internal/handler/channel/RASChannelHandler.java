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
package org.connectorio.addons.binding.canopen.ta.internal.handler.channel;

import java.util.concurrent.atomic.AtomicReference;
import javax.measure.Quantity;
import org.connectorio.addons.binding.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogObjectConfig;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.canopen.ta.tapi.val.IntAnalogValue;
import org.connectorio.addons.binding.canopen.ta.tapi.val.RASValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.units.indriya.quantity.Quantities;

public class RASChannelHandler extends BaseChannelHandler<RASValue, AnalogUnit, AnalogObjectConfig> {

  private final Logger logger = LoggerFactory.getLogger(RASChannelHandler.class);
  private AtomicReference<RASValue> rasValue = new AtomicReference<>();

  public RASChannelHandler(ThingHandlerCallback callback, TADevice device, Channel channel) {
    super(callback, device, channel, AnalogObjectConfig.class, RASValue.class);
  }

  @Override
  protected AnalogUnit determineUnit(AnalogObjectConfig thing) {
    return AnalogUnit.TEMPERATURE_REGULATOR;
  }

  @Override
  protected void registerInput(AnalogObjectConfig config, TADevice device) {
    RASValue ras = createFallback(config);
    TAAnalogInput input = new TAAnalogInput(device, (short) config.writeObjectIndex, (short) AnalogUnit.TEMPERATURE_REGULATOR.getIndex());
    input.update(ras);
    device.addAnalogInput(config.writeObjectIndex, input);
  }

  @Override
  protected void registerOutput(AnalogObjectConfig config, TADevice device) {
    RASValue ras = createFallback(config);
    TAAnalogOutput output = new TAAnalogOutput(device, config.readObjectIndex, AnalogUnit.TEMPERATURE_REGULATOR.getIndex(), ras.encode());
    output.update(ras);
    device.addAnalogOutput(config.readObjectIndex, output);
  }

  @Override
  public void handleCommand(Command command) {
    if (config.writeObjectIndex == 0) {
      logger.warn("Ignoring write request {} to channel {} cause write object index is not set", command, channel.getUID());
      return;
    }

    // push update to controller
    device.write(config.writeObjectIndex, createValue(command));
  }

  @Override
  protected RASValue createValue(Command command) {
    RASValue value = this.rasValue.get();
    if (value == null) {
      value = new RASValue(Quantities.getQuantity(0, AnalogUnit.CELSIUS.getUnit()), 0, AnalogUnit.TEMPERATURE_REGULATOR);
    }

    if (command instanceof QuantityType) {
      // temperature channel
      QuantityType<?> type = (QuantityType<?>) command;
      Quantity<?> quantity = Quantities.getQuantity(((QuantityType<?>) command).doubleValue(), type.getUnit());
      value = new RASValue(quantity, value.getMode(), AnalogUnit.TEMPERATURE_REGULATOR);
    } else if (command instanceof DecimalType) {
      // mode
      int mode = ((DecimalType) command).intValue();
      value = new RASValue(value.getValue(), mode, AnalogUnit.TEMPERATURE_REGULATOR);
    }

    this.rasValue.set(value);
    return value;
  }

  @Override
  public void accept(int index, RASValue value) {
    logger.debug("Received update of matching object {} with value {}", index, value);

    rasValue.set(value);

    if (callback != null) {
      ChannelUID mode = new ChannelUID(channel.getUID().getThingUID(), TACANopenBindingConstants.TA_ANALOG_RAS_MODE + "#analog-output_" + index);
      ChannelUID temperature = new ChannelUID(channel.getUID().getThingUID(), TACANopenBindingConstants.TA_ANALOG_RAS_TEMPERATURE + "#analog-output_" + index);

      State modeState = createState(value);
      State temperatureState = createTemperatureState(value);
      callback.stateUpdated(mode, modeState);
      callback.stateUpdated(temperature, temperatureState);

      logger.debug("Updating mode channel {} to {} from RAS {} and temperature channel {} to {} from RAS {}.",
        mode, modeState, value.getMode(), temperature, temperatureState, value.getValue());
    } else {
      logger.warn("Ignoring state update {} for {}, handler not ready", value, config.readObjectIndex);
    }
  }

  protected State createState(RASValue value) {
    return new DecimalType(value.getMode());
  }

  private State createTemperatureState(RASValue value) {
    Quantity<?> quantity = value.getValue();
    return QuantityType.valueOf(quantity.getValue().doubleValue(), quantity.getUnit());
  }

  private RASValue createFallback(AnalogObjectConfig config) {
    IntAnalogValue value = new IntAnalogValue(config.fallback, AnalogUnit.CELSIUS);
    return new RASValue(value.getValue(), 0, AnalogUnit.TEMPERATURE_REGULATOR);
  }

}
