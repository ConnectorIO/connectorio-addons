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

import java.util.Collections;
import org.apache.plc4x.java.ads.readwrite.AdsDataType;
import org.apache.plc4x.java.ads.tag.AdsTag;
import org.apache.plc4x.java.ads.tag.DirectAdsTag;
import org.apache.plc4x.java.ads.tag.SymbolicAdsTag;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest.Builder;
import org.apache.plc4x.java.api.types.PlcValueType;
import org.connectorio.addons.binding.amsads.internal.config.channel.DirectFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.SymbolicFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.TypedChannelConfiguration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AdsChannelHandlerBase implements AdsChannelHandler {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final Thing thing;
  protected final Channel channel;

  protected AdsChannelHandlerBase(Thing thing, Channel channel) {
    this.thing = thing;
    this.channel = channel;
  }

  protected void subscribe(Builder subscriptionBuilder, AdsTag tag, String channelId) {
    if (tag == null) {
      logger.warn("Could not determine valid subscription kind for channel {} with config {}", channel.getUID(), channel.getConfiguration().getProperties());
      return;
    }
    subscriptionBuilder.addChangeOfStateTag(channelId, tag);
  }

  protected AdsTag createTag(TypedChannelConfiguration typeCfg, DirectFieldConfiguration address) {
    return new DirectAdsTag(address.getIndexGroup(), address.getIndexOffset(), typeCfg.type.name(), 1);
  }

  protected AdsTag createTag(TypedChannelConfiguration typeCfg, SymbolicFieldConfiguration address) {
    PlcValueType dataType = ads2plc(typeCfg.type);
    if (dataType != null) {
      return new SymbolicAdsTag(address.getSymbol(), dataType, Collections.emptyList());
    }
    return null;
  }

  private PlcValueType ads2plc(AdsDataType adsDataType) {
    return PlcValueType.enumForValue(adsDataType.getPlcValueType().getValue());
  }

}
