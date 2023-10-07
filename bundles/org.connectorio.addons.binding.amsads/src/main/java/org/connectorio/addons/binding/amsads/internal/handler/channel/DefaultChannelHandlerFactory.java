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

import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;

public class DefaultChannelHandlerFactory implements ChannelHandlerFactory {

  @Override
  public AdsChannelHandler create(Thing thing, SymbolEntry symbol) {
    switch (symbol.getType()) {
      case BOOL:
      case BIT:
      case BIT8:
      case BYTE:
      case BITARR8:
        return new BinaryAdsChannelHandler(thing, symbol);
      case WORD:
      case BITARR16:
      case DWORD:
      case BITARR32:
      case SINT:
      case INT8:
      case USINT:
      case UINT8:
      case INT:
      case INT16:
      case UINT:
      case UINT16:
      case DINT:
      case INT32:
      case UDINT:
      case UINT32:
      case LINT:
      case INT64:
      case ULINT:
      case UINT64:
      case REAL:
      case FLOAT:
      case LREAL:
      case DOUBLE:
        return new NumericAdsChannelHandler(thing, symbol);
    }
    return null;
  }

  @Override
  public AdsChannelHandler map(Thing thing, Channel channel) {
    String channelId = channel.getChannelTypeUID().getId();
    if (channelId.startsWith("contact-") || channelId.startsWith("switch-")) {
      return new BinaryAdsChannelHandler(thing, channel);
    }
    if (channelId.startsWith("number-")) {
      return new NumericAdsChannelHandler(thing, channel);
    }
    return null;
  }
}
