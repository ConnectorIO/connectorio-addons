package org.connectorio.binding.plc4x.canopen.ta.internal.handler;

import java.util.Optional;
import java.util.function.Function;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAValue;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ThingChannelValueListener implements ValueListener {

  private final Logger logger = LoggerFactory.getLogger(ThingChannelValueListener.class);
  private final ThingHandlerCallback callback;
  private final Thing thing;
  private final Function<TAValue, State> converter;

  ThingChannelValueListener(ThingHandlerCallback callback, Thing thing, Function<TAValue, State> converter) {
    this.callback = callback;
    this.thing = thing;
    this.converter = converter;
  }

  @Override
  public void analog(int index, ReadBuffer buffer) throws ParseException {
    short val = buffer.readShort(16);
    Channel channel = thing.getChannel("analog#" + index);
    int unitIndex = Optional.ofNullable(channel.getConfiguration()).map(cfg -> cfg.get("unit"))
      .filter(Number.class::isInstance)
      .map(Number.class::cast)
      .map(Number::intValue)
      .orElse(0);

    TAValue value = new TAValue(unitIndex, val);

    logger.info("Analog channel {} (index {}) value {}", channel.getUID(), index, value);

    Optional.ofNullable(callback).ifPresent(callback -> callback.stateUpdated(channel.getUID(), converter.apply(value)));
  }

  @Override
  public void digital(int index, boolean value) {
    Channel channel = thing.getChannel("digital#" + index);

    if (channel != null) {
      logger.info("Digital channel {} (index {}) value {}", channel.getUID(), index, value);
      Optional.ofNullable(callback)
        .ifPresent(callback -> callback.stateUpdated(channel.getUID(), value ? OpenClosedType.OPEN : OpenClosedType.CLOSED));
    } else {
      logger.trace("Unknown digital channel digital#{}", index);
    }
  }
}
