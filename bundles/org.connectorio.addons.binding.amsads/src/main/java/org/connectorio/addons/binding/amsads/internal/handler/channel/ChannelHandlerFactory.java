package org.connectorio.addons.binding.amsads.internal.handler.channel;

import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.thing.Thing;

public interface ChannelHandlerFactory {

  AdsChannelHandler create(Thing thing, SymbolEntry symbol);

}
