package org.connectorio.binding.compute.cycle.internal.operation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.connectorio.binding.compute.cycle.internal.CycleOperation;
import org.connectorio.binding.compute.cycle.internal.config.CounterChannelConfig;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleCount implements CycleOperation {

  private final Logger logger = LoggerFactory.getLogger(CycleCount.class);
  private final Supplier<Long> clock;
  private final ThingHandlerCallback callback;
  private final ChannelUID channelUID;
  private final CounterChannelConfig config;
  private final AtomicLong count = new AtomicLong();

  private Long resetAfter;

  public CycleCount(Supplier<Long> clock, ThingHandlerCallback callback, ChannelUID channelUID, CounterChannelConfig config) {
    this.clock = clock;
    this.callback = callback;
    this.channelUID = channelUID;
    this.config = config;

    this.resetAfter = new TimeCalculator().calculateNextReset(clock, config.duration, config.unit);
  }

  @Override
  public void open() {
    if (clock.get() > resetAfter) {
      this.count.set(0);
      this.resetAfter = new TimeCalculator().calculateNextReset(clock, config.duration, config.unit);
    }
  }

  @Override
  public void close() {
    long cycle = count.incrementAndGet();
    logger.trace("Setting cycle count to {}", cycle);
    callback.stateUpdated(channelUID, new DecimalType(cycle
    ));
  }

  @Override
  public ChannelUID getChannelId() {
    return channelUID;
  }

}
