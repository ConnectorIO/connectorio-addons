/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.amsads.internal.handler;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.plc4x.java.ads.tag.AdsTag;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.connectorio.addons.binding.amsads.internal.config.AdsConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.AmsConfiguration;
import org.connectorio.addons.binding.amsads.internal.handler.channel.AdsChannelHandler;
import org.connectorio.addons.binding.amsads.internal.handler.channel.ChannelHandlerFactory;
import org.connectorio.addons.binding.amsads.internal.handler.polling.FetchContainer;
import org.connectorio.addons.binding.amsads.internal.handler.polling.PollFetchContainer;
import org.connectorio.addons.binding.amsads.internal.handler.polling.SubscribeFetchContainer;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReader;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReaderFactory;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top level type unifying handling of AMS/ADS bridges.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public abstract class AbstractAmsAdsThingHandler<B extends AmsBridgeHandler, C extends AdsConfiguration> extends GenericThingHandlerBase<B, C> {

  private final Logger logger = LoggerFactory.getLogger(AbstractAmsAdsThingHandler.class);
  private final SymbolReaderFactory symbolReaderFactory;
  private final ChannelHandlerFactory channelHandlerFactory;

  private final Map<String, Entry<AdsTag, AdsChannelHandler>> handlerMap = new ConcurrentHashMap<>();
  private final CompletableFuture<PlcConnection> initializer = new CompletableFuture<>();;

  private FetchContainer subscriber;
  private FetchContainer poller;

  public AbstractAmsAdsThingHandler(Thing thing, SymbolReaderFactory symbolReaderFactory, ChannelHandlerFactory channelHandlerFactory) {
    super(thing);
    this.symbolReaderFactory = symbolReaderFactory;
    this.channelHandlerFactory = channelHandlerFactory;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    String channelId = channelUID.getAsString();
    Entry<AdsTag, AdsChannelHandler> handlerEntry = handlerMap.get(channelId);
    if (handlerEntry == null) {
      logger.warn("Could not handle command '{}', unsupported channel {}", command, channelUID);
      return;
    }

    PlcValue value = handlerEntry.getValue().update(command);
    if (value == null) {
      logger.warn("Skip write channel {} write attempt with command {}, can not determine write value", channelId, command);
      return;
    }

    getPlcConnection().thenCompose(connection -> {
      logger.trace("Attempting to send channel {} command {}", channelId, command);
      PlcWriteRequest.Builder builder = connection.writeRequestBuilder();
      builder.addTag(channelId, handlerEntry.getKey(), value);
      return builder.build().execute().whenComplete((r, e) -> {
        if (e != null) {
          logger.warn("Failure while writing channel {} command {} to device.", channelId, command, e);
          return;
        }
        PlcResponseCode responseCode = r.getResponseCode(channelId);
        if (responseCode != PlcResponseCode.OK) {
          logger.warn("Error '{}' reported for channel {} write attempt with command {}", responseCode, channelId, command);
          return;
        }
        logger.debug("Successful write for channel {} with command {}", channelId, command);
      });
    });
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.UNKNOWN);

    if (getBridge() == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Please attach this bridge handler to AMS ADS network bridge.");
      return;
    }

    if (getBridge().getHandler() != null && !(getBridge().getHandler() instanceof AmsBridgeHandler)) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Unknown bridge handler used.");
      return;
    }

    Optional<AmsConfiguration> config = ((AmsBridgeHandler) getBridge().getHandler()).getBridgeConfig();
    if (!config.isPresent()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "AMS ADS network bridge is not configured yet.");
      return;
    }

    Runnable connectionTask = createInitializer(config.get(), initializer);
    scheduler.submit(connectionTask);
    initializer.whenComplete((connection, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection failed " + error.getMessage());
        initializer.complete(null);
        return;
      }

      Set<SymbolEntry> symbolEntries = Collections.emptySet();
      // populate raw symbol table, adjust bug in PLC4X itself
      SymbolReader reader = symbolReaderFactory.create(connection);

      if (getThingConfig().get().discoverChannels) {
        try {
          symbolEntries = reader.read().join();
        } catch (Exception e) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Could not retrieve data type and symbol information from ADS device " + e.getMessage());
          return;
        }

        try {
          updateChannels(symbolEntries);
        } catch (Exception e) {
          logger.warn("Failed to discover ADS channels", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Failure while reading channels definitions from ADS device " + e.getMessage());
          return;
        }
      }

      List<Channel> channels = getThing().getChannels();
      poller = new PollFetchContainer(scheduler, connection);
      subscriber = new SubscribeFetchContainer(connection);
      for (Channel channel : channels) {
        AdsChannelHandler handler = channelHandlerFactory.map(thing, getCallback(), channel);
        if (handler != null) {
          String channelId = channel.getUID().getAsString();
          AdsTag tag = handler.createTag();
          if (tag == null) {
            logger.warn("Ignoring channel {}, unsupported tag address", channelId);
            continue;
          }
          if (handler.getRefreshInterval() != null) {
            poller.add(handler.getRefreshInterval(), channelId, tag, handler::onChange);
          } else {
            subscriber.add(null, channelId, tag, handler::onChange);
          }
          // register handler so we can dispatch commands
          handlerMap.put(channelId, new SimpleEntry<>(tag, handler));
        }
      }

      if (channels.isEmpty()) {
        updateStatus(ThingStatus.ONLINE);
        return;
      }

      try {
        if (subscriber.start()) {
          logger.info("Started ADS subscription for thing {}", thing.getUID());
        }
        if (poller.start()) {
          logger.info("Started ADS polling for thing {}", thing.getUID());
        }
        updateStatus(ThingStatus.ONLINE);
      } catch (Exception e) {
        logger.error("Failed to initialize thing", e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Failed to initialize thing " + e.getMessage());
      }
    });
  }

  @Override
  public void dispose() {
    if (poller != null) {
      scheduler.execute(() -> {
        try {
          poller.stop();
        } catch (Exception e) {
          logger.warn("Failed to gracefully shutdown polling", e);
        }
      });
    }
    if (subscriber != null) {
      scheduler.execute(() -> {
        try {
          subscriber.stop();
        } catch (Exception e) {
          logger.warn("Failed to gracefully shutdown subscription", e);
        }
      });
    }
    initializer.thenAccept((connection) -> {
      try {
        if (connection != null && connection.isConnected()) {
          connection.close();
        }
      } catch (Exception e) {
        logger.warn("Failed to close connection", e);
      }
    });
    super.dispose();
  }

  private void updateChannels(Set<SymbolEntry> symbolEntries) {
    List<Channel> channels = new ArrayList<>();

    for (SymbolEntry symbolEntry : symbolEntries) {
      AdsChannelHandler handler = channelHandlerFactory.create(getThing(), symbolEntry);
      if (handler != null) {
        channels.add(handler.createChannel());
      }
    }

    updateThing(editThing().withChannels(channels).build());
  }

  protected abstract Runnable createInitializer(AmsConfiguration amsAds, CompletableFuture<PlcConnection> initializer);

  public final CompletableFuture<PlcConnection> getPlcConnection() {
    return initializer;
  }

}
