package org.connectorio.addons.binding.mbus.internal.handler.source;

import java.util.function.Consumer;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

public class MBusChannelCallback implements Consumer<State> {

  private final ThingHandlerCallback callback;
  private final Channel channel;

  public MBusChannelCallback(ThingHandlerCallback callback, Channel channel) {
    this.callback = callback;
    this.channel = channel;
  }

  @Override
  public void accept(State state) {
    callback.stateUpdated(channel.getUID(), state);
  }

}
