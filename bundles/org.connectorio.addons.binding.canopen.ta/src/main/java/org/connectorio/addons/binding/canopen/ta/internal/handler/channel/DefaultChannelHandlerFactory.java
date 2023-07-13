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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.connectorio.addons.binding.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.TADevice;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelHandlerFactory implements ChannelHandlerFactory {

  private final Logger logger = LoggerFactory.getLogger(DefaultChannelHandlerFactory.class);
  private final Map<Key, ChannelHandler<?, ?, ?>> handlerMap = new HashMap<>();

  @Override
  public ChannelHandler<?, ?, ?> create(ThingHandlerCallback callback, TADevice device, Channel channel) {
    logger.debug("Creating handler for channel {} from device {} config {}", prettyPrintChannel(channel), device, channel.getConfiguration());

//    InputOutputObjectConfig cfg = channel.getConfiguration().as(InputOutputObjectConfig.class);
//    Key key = new Key(device.getNode().getNodeId(), cfg.readObjectIndex, cfg.writeObjectIndex);
//    if (!handlerMap.containsKey(key)) {
//      handlerMap.put(key, createHandler(callback, device, channel));
//    }
    BaseChannelHandler<?, ?, ?> handler = createHandler(callback, device, channel);
    logger.debug("Created handler {}", handler);
    return handler;

//    return handlerMap.get(key);
  }

  private BaseChannelHandler<?, ?, ?> createHandler(ThingHandlerCallback callback, TADevice device, Channel channel) {
    String type = channel.getChannelTypeUID().getId();
    if (type.equals(TACANopenBindingConstants.TA_ANALOG_RAS_MODE) || type.equals(TACANopenBindingConstants.TA_ANALOG_RAS_TEMPERATURE)) {
      return new RASChannelHandler(callback, device, channel);
    }

    if (type.startsWith(TACANopenBindingConstants.TA_ANALOG_PREFIX)) {
      return new AnalogChannelHandler(callback, device, channel);
    }

    if (type.startsWith(TACANopenBindingConstants.TA_DIGITAL_PREFIX)) {
      return new DigitalChannelHandler(callback, device, channel);
    }

    throw new IllegalArgumentException("Unsupported channel type " + type);
  }

  private Object prettyPrintChannel(Channel channel) {
    // huh?
    return new Object() {
      @Override
      public String toString() {
        return "type: " + channel.getChannelTypeUID() + " (uid: " + channel.getUID() + ")";
      }
    };
  }

  static class Key {
    final int device;
    final int read;
    final int write;

    Key(int device, int read, int write) {
      this.device = device;
      this.read = read;
      this.write = write;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key)) {
        return false;
      }
      Key key = (Key) o;
      return device == key.device && read == key.read && write == key.write;
    }

    @Override
    public int hashCode() {
      return Objects.hash(device, read, write);
    }
  }
}
