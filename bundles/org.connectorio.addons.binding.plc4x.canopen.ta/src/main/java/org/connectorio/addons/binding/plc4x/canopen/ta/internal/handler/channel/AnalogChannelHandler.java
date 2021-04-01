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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel;

import javax.measure.Quantity;
import javax.measure.UnitConverter;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogChannelConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.TABaseObjectThingHandler;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.AnalogValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import tec.uom.se.quantity.Quantities;

public class AnalogChannelHandler extends BaseChannelHandler<AnalogValue, AnalogUnit, AnalogObjectConfig> {

  public AnalogChannelHandler(ThingHandlerCallback callback, TADevice device, Channel channel) {
    super(callback, device, channel, AnalogObjectConfig.class, AnalogValue.class);
  }

  @Override
  protected AnalogUnit determineUnit(AnalogObjectConfig config) {
    return config.unit;
  }

  @Override
  protected void registerInput(int writeObjectIndex, TADevice device) {
    device.addAnalogInput(writeObjectIndex, new TAAnalogInput(device, writeObjectIndex, unit.getIndex()));
  }

  @Override
  protected void registerOutput(int readObjectIndex, TADevice device) {
    device.addAnalogOutput(readObjectIndex, new TAAnalogOutput(device, readObjectIndex, unit.getIndex(), (short) 0));
  }

  protected Value<?> createValue(Command command) {
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

  protected State createState(AnalogValue value) {
    Quantity<?> quantity = value.getValue();
    return QuantityType.valueOf(quantity.getValue().doubleValue(), quantity.getUnit());
  }

}
