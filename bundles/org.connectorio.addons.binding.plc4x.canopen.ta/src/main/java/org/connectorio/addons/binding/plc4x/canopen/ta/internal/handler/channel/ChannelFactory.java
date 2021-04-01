package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

public interface ChannelFactory {

  <T extends Value<?>> CompletableFuture<List<Channel>> create(ThingUID thing, TACanInputOutputObject<T> object);

}
