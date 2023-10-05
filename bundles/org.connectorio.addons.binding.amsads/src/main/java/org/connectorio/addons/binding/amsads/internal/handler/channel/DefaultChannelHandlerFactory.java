package org.connectorio.addons.binding.amsads.internal.handler.channel;

import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
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
}
