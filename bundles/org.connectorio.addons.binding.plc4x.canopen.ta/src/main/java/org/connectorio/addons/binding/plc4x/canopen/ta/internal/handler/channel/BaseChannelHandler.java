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

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.InputOutputObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.FilterValueCallback;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseChannelHandler<T extends Value<?>, U extends TAUnit, C extends InputOutputObjectConfig>
  implements ChannelHandler<T, U, C> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final ThingHandlerCallback callback;
  protected final TADevice device;
  protected final Channel channel;
  private final Class<C> configType;
  private final Class<T> valueType;

  protected C config;
  protected U unit;
  private FilterValueCallback<T> valueCallback;

  protected BaseChannelHandler(ThingHandlerCallback callback, TADevice device, Channel channel, Class<C> configType, Class<T> valueType) {
    this.callback = callback;
    this.device = device;
    this.channel = channel;
    this.configType = configType;
    this.valueType = valueType;
  }

  @Override
  public void initialize() {
    this.config = getConfigAs(configType);

    unit = determineUnit(this.config);
    if (config.readObjectIndex != 0) {
      registerOutput(config.readObjectIndex, device);
      valueCallback = new FilterValueCallback<>(this, config.readObjectIndex, valueType);
      device.addValueCallback(valueCallback);
    }
    if (config.writeObjectIndex != 0) {
      registerInput(config.writeObjectIndex, device);
    }
  }

  public void dispose() {
    if (valueCallback != null) {
      device.removeValueCallback(valueCallback);
    }
  }

  protected C getConfigAs(Class<C> configType) {
    return channel.getConfiguration().as(configType);
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
  public void accept(int index, T value) {
    logger.debug("Received update of matching object {} with value {}", index, value);

    if (callback != null) {
      callback.stateUpdated(channel.getUID(), createState(value));
    } else {
      logger.warn("Ignoring state update {} for {}, handler not ready", value, config.readObjectIndex);
    }
  }

  protected abstract U determineUnit(C config);

  protected abstract void registerInput(int writeObjectIndex, TADevice device);
  protected abstract void registerOutput(int readObjectIndex, TADevice device);

  protected abstract Value<?> createValue(Command command);
  protected abstract State createState(T value);

  public String toString() {
    return getClass().getName() + "[" + device + " " + channel + " " + channel.getConfiguration() + " " + valueType + "]";
  }

}
