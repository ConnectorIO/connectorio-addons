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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider.TAChannelTypeProvider;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelFactory implements ChannelFactory {

  private final Logger logger = LoggerFactory.getLogger(DefaultChannelFactory.class);

  @Override
  public <T extends Value<?>> CompletableFuture<List<Channel>> create(ThingUID thing, TACanInputOutputObject<T> object, Integer inputKey) {
    logger.debug("Creating new channel(s) in {} from received object {}", thing, object);

    if (object instanceof TAAnalogOutput) {
      return getAnalogChannel(thing, object, inputKey);
    }
    if (object instanceof TAAnalogInput) {
      return getAnalogChannel(thing, object, inputKey);
    }
    if (object instanceof TADigitalOutput) {
      return getDigitalChannel(thing, object, inputKey);
    }
    if (object instanceof TADigitalInput) {
      return getDigitalChannel(thing, object, inputKey);
    }

    logger.debug("No matching channel found for thing {} and object {}", thing, object);
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  private <T extends Value<?>> CompletableFuture<List<Channel>> getAnalogChannel(ThingUID thing, TACanInputOutputObject<T> object, Integer inputKey) {
    AnalogUnit unit = AnalogUnit.valueOf(object.getUnit());
    if (unit == null) {
      logger.info("Ignoring analog I/O object {} object. Could not find matching channel and thing for unit {}", object, object.getUnit());
      return CompletableFuture.completedFuture(Collections.emptyList());
    }

    return object.getName().handle((name, error) -> {
      if (error != null) {
        logger.debug("Failed to read analog label", error);
        name = "Analog object #" + object.getIndex();
      }

      return createChannels(thing, object, unit, name, inputKey);
    });
  }

  private <T extends Value<?>> CompletableFuture<List<Channel>> getDigitalChannel(ThingUID thing, TACanInputOutputObject<T> object, Integer inputKey) {
    DigitalUnit unit = DigitalUnit.valueOf(object.getUnit());
    if (unit == null) {
      logger.info("Ignoring digital I/O object {} object. Could not find matching channel and thing for unit {}", object, object.getUnit());
      return CompletableFuture.completedFuture(Collections.emptyList());
    }

    return object.getName().handle((name, error) -> {
      if (error != null) {
        logger.debug("Failed to read digital label", error);
        name = "Digital object #" + object.getIndex();
      }

      return createChannels(thing, object, unit, name, inputKey);
    });
  }

  private List<Channel> createChannels(ThingUID thing, TACanInputOutputObject<?> object, TAUnit unit, String name, Integer inputKey) {
    return TAChannelTypeProvider.forObject(thing, object, unit, name, inputKey);
    /*
    List<Channel> channels = new ArrayList<>();
    for (ChannelTypeUID channelType : TAChannelTypeProvider.forObject(object, unit, name)) {
      Map<String, Object> configuration = new HashMap<>();
      configuration.put("readObjectIndex", object.getIndex());
      configuration.put("unit", unit.name());

      ChannelUID uid = new ChannelUID(thing,  channelType.getId() + "#" + object.getIndex());
      ChannelBuilder channelBuilder = ChannelBuilder.create(uid)
        .withLabel(name)
        .withType(channelType)
        .withConfiguration(new Configuration(configuration));

      channels.add(channelBuilder.build());
    }
    return channels;
    //*/
  }

}
