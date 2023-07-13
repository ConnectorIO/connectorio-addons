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
package org.connectorio.addons.binding.canopen.ta.internal.handler.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.connectorio.addons.binding.canopen.ta.internal.config.DeviceConfig;
import org.connectorio.addons.binding.canopen.ta.internal.handler.builder.linking.InputObjectLinkStrategy;
import org.connectorio.addons.binding.canopen.ta.internal.handler.builder.linking.ObjectKey;
import org.connectorio.addons.binding.canopen.ta.internal.handler.channel.DefaultChannelFactory;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TACanOutputObject;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardDeviceChannelBuilder implements DeviceChannelBuilder {

  private final Logger logger = LoggerFactory.getLogger(StandardDeviceChannelBuilder.class);

  // define maximum index of an object which should be checked
  // limits are not equal - UVR 16x2 can have up to 64 inputs but only 32 outputs
  // however matching should make sure that all options are verified
  // private final int digitalObjectMax = 64;
  // private final int analogObjectMax = 64;

  private final ThingUID uid;
  private final BridgeBuilder builder;
  private final DeviceConfig config;

  private final InputObjectLinkStrategy strategy;
  private final List<TACanInputOutputObject<?>> objects = new ArrayList<>();

  public StandardDeviceChannelBuilder(ThingUID uid, BridgeBuilder builder, DeviceConfig config, InputObjectLinkStrategy strategy) {
    this.uid = uid;
    this.builder = builder;
    this.config = config;
    this.strategy = strategy;
  }

  @Override
  public CompletableFuture<Bridge> build() {
    DefaultChannelFactory channelFactory = new DefaultChannelFactory();

    List<TACanInputOutputObject<?>> inputs = objects.stream()
      .filter(type -> !(type instanceof TACanOutputObject))
      .collect(Collectors.toList());

    List<CompletableFuture<?>> futures = new ArrayList<>();
    List<ObjectKey> matchedInputs = new ArrayList<>();
    for (TACanInputOutputObject<?> object : objects) {
      if (object instanceof TACanOutputObject) {
        logger.debug("Looking for {} matches", object);
        CompletableFuture<ObjectKey> matchingInput = strategy.get((TACanOutputObject<?>) object, inputs);
        CompletableFuture<List<Channel>> objectChannels = matchingInput.thenCompose((input) -> {
          // we match outputs to inputs, not vice versa
          if (input.getIndex() != -1) {
            logger.debug("Output {} name matches with input {}", object, input.getIndex());
            matchedInputs.add(input);
          }

          return channelFactory.create(uid, object, input.getIndex()).whenComplete(appendChannels(object));
        });
        futures.add(objectChannels);
      }
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(r -> {
      for (TACanInputOutputObject<?> object : objects) {
        if (object instanceof TACanOutputObject || matchedInputs.contains(new ObjectKey(object.getClass(), object.getIndex()))) {
          continue;
        }

        channelFactory.create(uid, object, -1).whenComplete(appendChannels(object));
      }
      return r;
    }).thenApply((r) -> builder.build());
  }

  private BiConsumer<List<Channel>, Throwable> appendChannels(TACanInputOutputObject<?> object) {
    return (channels, error) -> {
      if (error != null) {
        logger.warn("Could not initialize channel for object {}", object, error);
        return;
      }

      for (Channel channel : channels) {
        logger.info("Creating channel {} for object {}", channel, object);
        builder.withoutChannel(channel.getUID()).withChannel(channel);
      }
    };
  }

  @Override
  public DeviceChannelBuilder withConfiguration(Configuration configuration) {
    builder.withConfiguration(configuration);
    return this;
  }

  @Override
  public DeviceChannelBuilder withProperties(Map<String, String> properties) {
    builder.withProperties(properties);
    return this;
  }

  @Override
  public void accept(TACanInputOutputObject<?> object) {
    logger.info("Discovered new device level I/O object {}", object);

    objects.add(object);
  }

}
