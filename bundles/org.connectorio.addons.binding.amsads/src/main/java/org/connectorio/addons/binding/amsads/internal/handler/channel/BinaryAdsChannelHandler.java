package org.connectorio.addons.binding.amsads.internal.handler.channel;

import java.util.Map;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolEntry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

public class BinaryAdsChannelHandler implements AdsChannelHandler {

  private final Thing thing;
  private final SymbolEntry symbol;

  public BinaryAdsChannelHandler(Thing thing, SymbolEntry symbol) {
    this.thing = thing;
    this.symbol = symbol;
  }

  @Override
  public Channel createChannel() {
    return ChannelBuilder.create(new ChannelUID(thing.getUID(), Long.toHexString(symbol.getIndex()) + "x" + Long.toHexString(symbol.getOffset())))
      .withType(symbol.isReadOnly() ? AdsChannelHandler.CONTACT_SYMBOL : AdsChannelHandler.SWITCH_SYMBOL)
      .withAcceptedItemType(symbol.isReadOnly() ? CoreItemFactory.CONTACT : CoreItemFactory.SWITCH)
      .withLabel(symbol.getName())
      .withDescription(symbol.getDescription())
      .withConfiguration(new Configuration(Map.of(
        "symbol", symbol.getName(),
        "type", symbol.getType().name()
      )))
      .build();
  }

}
