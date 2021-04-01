package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel;

import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;

public interface ChannelHandlerFactory {

  ChannelHandler<?, ?, ?> create(ThingHandlerCallback callback, TADevice device, Channel channel);

}
