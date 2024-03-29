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

import org.apache.plc4x.java.ads.readwrite.PlcValueType;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;

public class DefaultChannelHandlerFactory implements ChannelHandlerFactory {

  @Override
  public AdsChannelHandler create(Thing thing, SymbolEntry symbol) {
    PlcValueType type = symbol.getType();
    if (type == null) {
      return null;
    }

    switch (type) {
      case NULL:
        break;
      case BOOL:
        return new BinaryAdsChannelHandler(thing, symbol);
      case BYTE:
      case LWORD:
      case WORD:
      case DWORD:
      case SINT:
      case USINT:
      case INT:
      case UINT:
      case DINT:
      case UDINT:
      case LINT:
      case ULINT:
      case REAL:
      case LREAL:
        return new NumericAdsChannelHandler(thing, symbol);
      case CHAR:
      case WCHAR:
      case STRING:
      case WSTRING:
        return new TextAdsChannelHandler(thing, symbol);
      case LDATE:
        break;
      case LTIME_OF_DAY:
        break;
      case TIME:
      case LTIME:
      case DATE:
      case TIME_OF_DAY:
      case DATE_AND_TIME:
      case LDATE_AND_TIME:
        return new DateTimeAdsChannelHandler(thing, symbol);
    }
    return null;
  }

  @Override
  public AdsChannelHandler map(Thing thing, ThingHandlerCallback callback, Channel channel) {
    String channelId = channel.getChannelTypeUID().getId();
    if (channelId.startsWith("contact-") || channelId.startsWith("switch-")) {
      return new BinaryAdsChannelHandler(thing, callback, channel);
    }
    if (channelId.startsWith("number-")) {
      return new NumericAdsChannelHandler(thing, callback, channel);
    }
    if (channelId.startsWith("datetime-")) {
      return new DateTimeAdsChannelHandler(thing, callback, channel);
    }
    if (channelId.startsWith("text-")) {
      return new TextAdsChannelHandler(thing, callback, channel);
    }
    return null;
  }
}
