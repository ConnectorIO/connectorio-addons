package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.InputOutputObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.TADevice;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;

public class DefaultChannelHandlerFactory implements ChannelHandlerFactory {

  private Map<Integer, ChannelHandler<?, ?, ?>> handlerMap = new ConcurrentHashMap<>();

  @Override
  public ChannelHandler<?, ?, ?> create(ThingHandlerCallback callback, TADevice device, Channel channel) {
    String type = channel.getChannelTypeUID().getId();

    InputOutputObjectConfig config = channel.getConfiguration().as(InputOutputObjectConfig.class);

    if (type.equals(TACANopenBindingConstants.TA_ANALOG_RAS_MODE)) {
      if (!handlerMap.containsKey(config.readObjectIndex)) {
        handlerMap.put(config.readObjectIndex, new RASChannelHandler(callback, device, channel));
      }
      return handlerMap.get(config.readObjectIndex);
    }

    if (type.startsWith(TACANopenBindingConstants.TA_ANALOG_PREFIX)) {
      return new AnalogChannelHandler(callback, device, channel);
    }

    if (type.startsWith(TACANopenBindingConstants.TA_DIGITAL_PREFIX)) {
      return new DigitalChannelHandler(callback, device, channel);
    }

    throw new IllegalArgumentException("Unsupported channel type " + type);
  }

}
