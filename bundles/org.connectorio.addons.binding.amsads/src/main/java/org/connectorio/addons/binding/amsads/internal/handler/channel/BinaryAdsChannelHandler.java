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
package org.connectorio.addons.binding.amsads.internal.handler.channel;

import java.util.Map;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest.Builder;
import org.connectorio.addons.binding.amsads.internal.config.channel.binary.BinaryDirectDecimalFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.binary.BinaryDirectHexFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.binary.BinarySymbolicFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

public class BinaryAdsChannelHandler extends AdsChannelHandlerBase implements AdsChannelHandler {

  private final SymbolEntry symbol;

  public BinaryAdsChannelHandler(Thing thing, SymbolEntry symbol) {
    this(thing, null, null, symbol);
  }

  public BinaryAdsChannelHandler(Thing thing, ThingHandlerCallback callback, Channel channel) {
    this(thing, callback, channel, null);
  }

  private BinaryAdsChannelHandler(Thing thing, ThingHandlerCallback callback, Channel channel, SymbolEntry symbol) {
    super(thing, callback, channel);
    this.symbol = symbol;
  }

  @Override
  public Channel createChannel() {
    return ChannelBuilder.create(new ChannelUID(thing.getUID(), Long.toHexString(symbol.getIndex()) + "x" + Long.toHexString(symbol.getOffset())))
      //.withType(symbol.isReadOnly() ? AdsChannelHandler.CONTACT_DIRECT_HEX : AdsChannelHandler.SWITCH_DIRECT_HEX)
      .withType(symbol.isReadOnly() ? AdsChannelHandler.CONTACT_SYMBOL : AdsChannelHandler.SWITCH_SYMBOL)
      .withAcceptedItemType(symbol.isReadOnly() ? CoreItemFactory.CONTACT : CoreItemFactory.SWITCH)
      .withLabel(symbol.getName())
      .withDescription(symbol.getDescription())
      .withConfiguration(new Configuration(Map.of(
//        "indexGroup", Long.toHexString(symbol.getIndex()),
//        "indexOffset", Long.toHexString(symbol.getOffset()),
        "symbol", symbol.getName(),
        "type", symbol.getType().name()
      )))
      .build();
  }

  @Override
  public void subscribe(Builder subscriptionBuilder, String channelId) {
    if (CONTACT_DIRECT_DEC.equals(channel.getChannelTypeUID())) {
      BinaryDirectDecimalFieldConfiguration configuration = channel.getConfiguration().as(BinaryDirectDecimalFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    } else if (CONTACT_DIRECT_HEX.equals(channel.getChannelTypeUID())) {
      BinaryDirectHexFieldConfiguration configuration = channel.getConfiguration().as(BinaryDirectHexFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    } else if (CONTACT_SYMBOL.equals(channel.getChannelTypeUID())) {
      BinarySymbolicFieldConfiguration configuration = channel.getConfiguration().as(BinarySymbolicFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    } else if (SWITCH_DIRECT_HEX.equals(channel.getChannelTypeUID())) {
      BinaryDirectDecimalFieldConfiguration configuration = channel.getConfiguration().as(BinaryDirectDecimalFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    } else if (SWITCH_DIRECT_DEC.equals(channel.getChannelTypeUID())) {
      BinaryDirectHexFieldConfiguration configuration = channel.getConfiguration().as(BinaryDirectHexFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    } else if (SWITCH_SYMBOL.equals(channel.getChannelTypeUID())) {
      BinarySymbolicFieldConfiguration configuration = channel.getConfiguration().as(BinarySymbolicFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    }
  }

  @Override
  public void onChange(Object value) {
    if (CoreItemFactory.CONTACT.equals(channel.getAcceptedItemType())) {
      callback.stateUpdated(channel.getUID(), Boolean.TRUE.equals(value) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
    }
    callback.stateUpdated(channel.getUID(), Boolean.TRUE.equals(value) ? OnOffType.ON : OnOffType.OFF);
  }
}
