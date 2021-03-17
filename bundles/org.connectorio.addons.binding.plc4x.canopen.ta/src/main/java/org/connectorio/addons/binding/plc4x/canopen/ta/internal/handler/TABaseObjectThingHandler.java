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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.InputOutputObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.ValueCallback;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TABaseObjectThingHandler<T extends Value<?>, U extends TAUnit, C extends InputOutputObjectConfig> extends BaseThingHandler implements ValueCallback<T> {

  public static final String OUTPUT = "output";
  public static final String INPUT = "input";

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final Class<C> configType;
  private final Class<T> valueType;

  protected C config;
  protected TADevice device;
  protected U unit;

  protected TABaseObjectThingHandler(Thing thing, Class<C> configType, Class<T> valueType) {
    super(thing);
    this.configType = configType;
    this.valueType = valueType;
  }

  @Override
  public void initialize() {
    TADeviceThingHandler handler = Optional.ofNullable(getBridge()).map(Bridge::getHandler)
      .filter(TADeviceThingHandler.class::isInstance)
      .map(TADeviceThingHandler.class::cast)
      .orElse(null);

    this.config = getConfigAs(configType);

    if (handler == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
      return;
    }

    // case for first run of discovered thing - user didn't have chances to set read/write object identifiers
    // but these are known to discovery process
    boolean modified = false;
    Map<String, String> properties = new LinkedHashMap<>(getThing().getProperties());
    Map<String, Object> configuration = new LinkedHashMap<>(getThing().getConfiguration().getProperties());
    ThingBuilder thingBuilder = editThing();
    if (config.readObjectIndex == 0 && properties.containsKey(OUTPUT)) {
      int outputId = readProperty(properties, OUTPUT);
      configuration.put("readObjectIndex", outputId);
      thingBuilder.withProperties(properties);
      thingBuilder.withConfiguration(new Configuration(configuration));
      modified = true;
    }
    if (config.writeObjectIndex == 0 && properties.containsKey(INPUT)) {
      int inputId = readProperty(properties, INPUT);
      configuration.put("writeObjectIndex", inputId);
      thingBuilder.withProperties(properties);
      thingBuilder.withConfiguration(new Configuration(configuration));
      modified = true;
    }

    if (modified) {
      updateThing(thingBuilder.build());
      this.config = getConfigAs(configType);
    }

    handler.getDevice().whenComplete((result, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, error.getMessage());
        return;
      }

      unit = determineUnit(getThing());
      this.device = result;

      if (config.readObjectIndex != 0) {
        registerOutput(config.readObjectIndex, device);
        result.addValueCallback(new FilterValueCallback<>(this, config.readObjectIndex, valueType));
      }
      if (config.writeObjectIndex != 0) {
        registerInput(config.writeObjectIndex, device);
      }

      updateStatus(ThingStatus.ONLINE);
    });
  }

  private int readProperty(Map<String, String> properties, String output) {
    String value = properties.remove(output);
    try {
      return ((Double) Double.parseDouble(value)).intValue();
    } catch (NumberFormatException e) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException ex) {

      }
    }
    return 0;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
   if (config.writeObjectIndex == 0) {
      logger.warn("Ignoring write request {} to channel {} cause write object index is not set", command, channelUID);
      return;
    }

    // push update to controller
    device.write(config.writeObjectIndex, createValue(command));
  }

  @Override
  public void accept(int index, T value) {
    logger.debug("Received update of matching object {} with value {}", index, value);

    ThingHandlerCallback callback = getCallback();
    if (callback != null) {
      Thing thing = getThing();
      Channel channel = thing.getChannel(thing.getThingTypeUID().getId());
      if (channel != null) {
        callback.stateUpdated(channel.getUID(), createState(value));
      }
    } else {
      logger.warn("Ignoring state update {} for {}, handler not ready", value, config.readObjectIndex);
    }
  }

  protected abstract U determineUnit(Thing thing);

  protected abstract void registerInput(int writeObjectIndex, TADevice device);
  protected abstract void registerOutput(int readObjectIndex, TADevice device);

  protected abstract Value<?> createValue(Command command);
  protected abstract State createState(T value);

}
