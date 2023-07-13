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

import org.connectorio.addons.binding.canopen.ta.internal.config.DigitalObjectConfig;
import org.connectorio.addons.binding.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TADigitalInput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TADigitalOutput;
import org.connectorio.addons.binding.canopen.ta.tapi.val.DigitalValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class DigitalChannelHandler extends BaseChannelHandler<DigitalValue, DigitalUnit, DigitalObjectConfig> {

  public DigitalChannelHandler(ThingHandlerCallback callback, TADevice device, Channel channel) {
    super(callback, device, channel, DigitalObjectConfig.class, DigitalValue.class);
  }

  @Override
  protected DigitalUnit determineUnit(DigitalObjectConfig thing) {
    return DigitalUnit.CLOSE_OPEN;
  }

  @Override
  protected void registerInput(DigitalObjectConfig config, TADevice device) {
    TADigitalInput input = new TADigitalInput(device, config.writeObjectIndex, DigitalUnit.CLOSE_OPEN.getIndex());
    input.update(new DigitalValue(config.fallback, DigitalUnit.CLOSE_OPEN));
    device.addDigitalInput(config.writeObjectIndex, input);
  }

  @Override
  protected void registerOutput(DigitalObjectConfig config, TADevice device) {
    device.addDigitalOutput(config.readObjectIndex, new TADigitalOutput(device, config.readObjectIndex, DigitalUnit.CLOSE_OPEN.getIndex(), config.fallback));
  }

  protected DigitalValue createValue(Command command) {
    if (command == OnOffType.ON || command == OpenClosedType.OPEN) {
      return new DigitalValue(true, unit);
    }
    return new DigitalValue(false, unit);
  }

  protected State createState(DigitalValue value) {
    return value.getValue() ? OnOffType.ON : OnOffType.OFF;
  }

}
