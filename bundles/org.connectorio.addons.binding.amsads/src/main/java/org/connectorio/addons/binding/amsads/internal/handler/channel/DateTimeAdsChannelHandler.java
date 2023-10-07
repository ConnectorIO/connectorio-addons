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
import org.connectorio.addons.binding.amsads.internal.config.channel.DirectDecimalFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.DirectHexFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.SymbolFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

public class DateTimeAdsChannelHandler extends AdsChannelHandlerBase implements AdsChannelHandler {

  private final SymbolEntry symbol;

  public DateTimeAdsChannelHandler(Thing thing, SymbolEntry symbol) {
    this(thing, null, symbol);
  }

  public DateTimeAdsChannelHandler(Thing thing, Channel channel) {
    this(thing, channel, null);
  }

  private DateTimeAdsChannelHandler(Thing thing, Channel channel, SymbolEntry symbol) {
    super(thing, channel);
    this.symbol = symbol;
  }



  @Override
  public Channel createChannel() {
    return ChannelBuilder.create(new ChannelUID(thing.getUID(), Long.toHexString(symbol.getIndex()) + "x" + Long.toHexString(symbol.getOffset())))
      .withType(AdsChannelHandler.DATETIME_DIRECT_HEX)
      .withAcceptedItemType(CoreItemFactory.DATETIME)
      .withLabel(symbol.getName())
      .withDescription(symbol.getDescription())
      .withConfiguration(new Configuration(Map.of(
        "symbol", symbol.getName(),
        "type", symbol.getType().name()
      )))
      .build();
  }

  @Override
  public void subscribe(Builder subscriptionBuilder, String channelId) {
    if (DATETIME_DIRECT_HEX.equals(channel.getChannelTypeUID())) {
      DirectDecimalFieldConfiguration configuration = channel.getConfiguration().as(DirectDecimalFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    } else if (DATETIME_DIRECT_HEX.equals(channel.getChannelTypeUID())) {
      DirectHexFieldConfiguration configuration = channel.getConfiguration().as(DirectHexFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    } else if (DATETIME_SYMBOL.equals(channel.getChannelTypeUID())) {
      SymbolFieldConfiguration configuration = channel.getConfiguration().as(SymbolFieldConfiguration.class);
      subscribe(subscriptionBuilder, createTag(configuration, configuration), channelId);
    }
  }

}
