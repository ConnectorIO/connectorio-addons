package org.connectorio.binding.compute.cycle.internal.operation;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.connectorio.binding.compute.cycle.internal.CycleOperation;
import org.connectorio.binding.compute.cycle.internal.config.CycleCounterConfig;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.quantity.time.TimeQuantities;

public class CycleTime implements CycleOperation {

  private final Logger logger = LoggerFactory.getLogger(CycleTime.class);

  private final Supplier<Long> clock;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;
  private final CycleCounterConfig config;
  private final AtomicLong start = new AtomicLong();

  public CycleTime(Supplier<Long> clock, ThingHandlerCallback callback, ChannelUID channelUID, CycleCounterConfig config) {
    this.clock = clock;
    this.callback = callback;
    this.channelUID = channelUID;
    this.config = config;
  }

  @Override
  public void open() {
    start.set(clock.get());
  }

  @Override
  public void close() {
    Long stop = clock.get();
    long cycleTime = stop - start.get();
    logger.trace("Calculated cycle time {} ({} - {})", cycleTime, stop, start.get());
    callback.stateUpdated(channelUID, QuantityType.valueOf(cycleTime, TimeQuantities.MILLISECOND));
  }

  @Override
  public ChannelUID getChannelId() {
    return channelUID;
  }

}
