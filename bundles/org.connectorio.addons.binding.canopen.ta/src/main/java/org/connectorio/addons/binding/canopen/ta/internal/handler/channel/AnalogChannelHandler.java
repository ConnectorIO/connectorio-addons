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

import javax.measure.Quantity;
import javax.measure.UnitConverter;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogObjectConfig;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.canopen.ta.tapi.val.AnalogValue;
import org.connectorio.addons.binding.canopen.ta.tapi.val.IntAnalogValue;
import org.connectorio.addons.binding.canopen.ta.tapi.val.Value;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import tech.units.indriya.quantity.Quantities;

public class AnalogChannelHandler extends BaseChannelHandler<AnalogValue, AnalogUnit, AnalogObjectConfig> {

  public AnalogChannelHandler(ThingHandlerCallback callback, TADevice device, Channel channel) {
    super(callback, device, channel, AnalogObjectConfig.class, AnalogValue.class);
  }

  @Override
  protected AnalogUnit determineUnit(AnalogObjectConfig config) {
    return config.unit;
  }

  @Override
  protected void registerInput(AnalogObjectConfig config, TADevice device) {
    TAAnalogInput input = new TAAnalogInput(device, config.writeObjectIndex, unit.getIndex());
    input.update(new IntAnalogValue(config.fallback, unit));
    device.addAnalogInput(config.writeObjectIndex, input);
  }

  @Override
  protected void registerOutput(AnalogObjectConfig config, TADevice device) {
    AnalogValue value = new IntAnalogValue(config.fallback, config.unit);
    TAAnalogOutput output = new TAAnalogOutput(device, config.readObjectIndex, unit.getIndex(), value.encode());
    device.addAnalogOutput(config.readObjectIndex, output);
  }

  protected Value<?> createValue(Command command) {
    logger.debug("Received command of type {} evaluated to {}", command.getClass(), command);
    if (command instanceof QuantityType) {
      QuantityType quantity = (QuantityType<?>) command;

      if (quantity.getUnit().isCompatible(unit.getUnit())) {
        logger.debug("Casting quantity to controller target unit {}", unit.getUnit());
        UnitConverter converter = quantity.getUnit().getConverterTo(unit.getUnit());
        if (converter != null) {
          double convert = converter.convert(quantity.doubleValue());
          logger.debug("Value converted to {}", convert);
          return new IntAnalogValue(Quantities.getQuantity(convert, unit.getUnit()), unit);
        }
      } else {
        double convert = quantity.doubleValue();
        logger.debug("Incompatible units detected, converting value to decimal {}", convert);
        return new IntAnalogValue(Quantities.getQuantity(convert, unit.getUnit()), unit);
      }
    }

    if (command instanceof Number) {
      logger.debug("Received command is not quantity, but still a number {}", command);
      return new IntAnalogValue(Quantities.getQuantity((Number) command, unit.getUnit()), unit);
    }

    logger.warn("Unsupported value type {}", command.getClass());
    return null;
  }

  protected State createState(AnalogValue value) {
    Quantity<?> quantity = value.getValue();
    return QuantityType.valueOf(quantity.getValue().doubleValue(), quantity.getUnit());
  }

}
