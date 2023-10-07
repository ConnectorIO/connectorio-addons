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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest.Builder;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.spi.values.PlcBOOL;
import org.connectorio.addons.binding.amsads.internal.config.AmsConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.AdsConfiguration;
import org.connectorio.addons.binding.amsads.internal.handler.channel.AdsChannelHandler;
import org.connectorio.addons.binding.amsads.internal.handler.channel.ChannelHandlerFactory;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReader;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReaderFactory;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

  private final Map<String, AdsChannelHandler> handlerMap = new ConcurrentHashMap<String, AdsChannelHandler>();
  private CompletableFuture<PlcConnection> initializer = new CompletableFuture<>();;

  public AbstractAmsAdsThingHandler(Thing thing, SymbolReaderFactory symbolReaderFactory, ChannelHandlerFactory channelHandlerFactory) {
    super(thing);
    this.symbolReaderFactory = symbolReaderFactory;
    this.channelHandlerFactory = channelHandlerFactory;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
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

      if (getThingConfig().get().discoverChannels) {
        SymbolReader reader = symbolReaderFactory.create(connection);
        try {
          Set<SymbolEntry> symbolEntries = reader.read().join();
          updateChannels(symbolEntries);
        } catch (Exception e) {
          logger.warn("Failed to discover ADS channels", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Failure while reading channels definitions from ADS device " + e.getMessage());
          return;
        }
      }

      List<Channel> channels = getThing().getChannels();
      Builder subscriptionBuilder = connection.subscriptionRequestBuilder();
      for (Channel channel : channels) {
        AdsChannelHandler handler = channelHandlerFactory.map(thing, channel);
        if (handler != null) {
          handlerMap.put(channel.getUID().getAsString(), handler);
          handler.subscribe(subscriptionBuilder);
        }
      }

      subscriptionBuilder.build().execute().whenComplete((r, e) -> {
        if (e != null) {
          logger.error("Failed to setup subscription within PLC", e);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Failure while subscribing for data subscribe " + e.getMessage());
          return;
        }
        for (String channelId : r.getTagNames()) {
          AdsChannelHandler handler = handlerMap.get(channelId);
          if (handler == null) {
            logger.warn("Received update for unknown channel {} in thing {}", channelId, thing.getUID());
            continue;
          }
          r.getSubscriptionHandle(channelId).register(new Consumer<PlcSubscriptionEvent>() {
            @Override
            public void accept(PlcSubscriptionEvent plcSubscriptionEvent) {
              Object value = plcSubscriptionEvent.getObject(channelId);
              if (value != null) {
                logger.debug("Channel {} received update {}", channelId, value);
                getCallback().stateUpdated(new ChannelUID(channelId), convert(value));
              }
            }
          });
        }

        updateStatus(ThingStatus.ONLINE);
      });
    });
  }

  private State convert(Object value) {
    if (value == null) {
      return UnDefType.NULL;
    }
    if (value instanceof PlcValue) {
      if (value instanceof PlcBOOL) {
        return ((PlcBOOL) value).getBoolean() ? OnOffType.ON : OnOffType.OFF;
      }
      return new DecimalType(((PlcValue) value).getBigDecimal());
    }

    // missing mapping
    return UnDefType.UNDEF;
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
