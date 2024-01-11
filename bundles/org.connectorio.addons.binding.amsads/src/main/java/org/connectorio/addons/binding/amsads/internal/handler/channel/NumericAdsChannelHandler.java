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

import java.math.BigDecimal;
import java.util.Map;
import org.apache.plc4x.java.ads.tag.AdsTag;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.spi.values.*;
import org.connectorio.addons.binding.amsads.internal.config.channel.TypedChannelConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.binary.BinaryDirectDecimalFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.binary.BinaryDirectHexFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.channel.binary.BinarySymbolicFieldConfiguration;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class NumericAdsChannelHandler extends AdsChannelHandlerBase implements AdsChannelHandler {

  private final SymbolEntry symbol;

  public NumericAdsChannelHandler(Thing thing, SymbolEntry symbol) {
    this(thing, null, null, symbol);
  }

  public NumericAdsChannelHandler(Thing thing, ThingHandlerCallback callback, Channel channel) {
    this(thing, callback, channel, null);
  }

  public NumericAdsChannelHandler(Thing thing, ThingHandlerCallback callback, Channel channel, SymbolEntry symbol) {
    super(thing, callback, channel);
    this.symbol = symbol;
  }

  @Override
  public Channel createChannel() {
    return ChannelBuilder.create(new ChannelUID(thing.getUID(), Long.toHexString(symbol.getIndex()) + "x" + Long.toHexString(symbol.getOffset())))
      //.withType(AdsChannelHandler.NUMBER_DIRECT_HEX)
      .withType(AdsChannelHandler.NUMBER_SYMBOL)
      .withAcceptedItemType(CoreItemFactory.NUMBER)
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
  public AdsTag createTag() {
    if (NUMBER_DIRECT_DEC.equals(channel.getChannelTypeUID())) {
      BinaryDirectDecimalFieldConfiguration configuration = channel.getConfiguration().as(BinaryDirectDecimalFieldConfiguration.class);
      return createTag(configuration, configuration);
    } else if (NUMBER_DIRECT_HEX.equals(channel.getChannelTypeUID())) {
      BinaryDirectHexFieldConfiguration configuration = channel.getConfiguration().as(BinaryDirectHexFieldConfiguration.class);
      return createTag(configuration, configuration);
    } else if (NUMBER_SYMBOL.equals(channel.getChannelTypeUID())) {
      BinarySymbolicFieldConfiguration configuration = channel.getConfiguration().as(BinarySymbolicFieldConfiguration.class);
      return createTag(configuration, configuration);
    }
    return null;
  }

  @Override
  public void onChange(Object value) {
    if (value instanceof Long) {
      callback.stateUpdated(channel.getUID(), new DecimalType((Long) value));
    } else if (value instanceof Integer) {
      callback.stateUpdated(channel.getUID(), new DecimalType((Integer) value));
    } else if (value instanceof Short) {
      callback.stateUpdated(channel.getUID(), new DecimalType((Short) value));
    } else if (value instanceof BigDecimal) {
      callback.stateUpdated(channel.getUID(), new DecimalType((BigDecimal) value));
    } else if (value instanceof Double) {
      callback.stateUpdated(channel.getUID(), new DecimalType((Double) value));
    } else if (value instanceof Float) {
      callback.stateUpdated(channel.getUID(), new DecimalType((Float) value));
    } else {
      callback.stateUpdated(channel.getUID(), new DecimalType(value.toString()));
    }
  }

  @Override
  public PlcValue update(Command command) {
    if (!(command instanceof State)) {
      return null;
    }

    DecimalType value = ((State) command).as(DecimalType.class);
    TypedChannelConfiguration config = channel.getConfiguration().as(TypedChannelConfiguration.class);
    switch (config.type) {
      case BIT:
      case BOOL:
        return new PlcBOOL(value.intValue());
      case BYTE:
        return new PlcBYTE(value.shortValue());
      case WORD:
        return new PlcWORD(value.intValue());
      case DWORD:
      case SINT:
      case INT8:
      case USINT:
      case UINT8:
      case INT:
      case INT16:
      case UINT:
      case UINT16:
        return new PlcUINT(value.intValue());
      case DINT:
      case INT32:
      case UDINT:
      case UINT32:
      case LINT:
        return new PlcDINT(value.longValue());
      case INT64:
      case ULINT:
      case UINT64:
        return new PlcULINT(value.toBigDecimal());
      case REAL:
      case FLOAT:
      case LREAL:
      case DOUBLE:
        return new PlcREAL(value.doubleValue());
    }
    return null;
  }
}
