package org.connectorio.binding.compute.cycle.internal.operation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimeCalculator {

  public Long calculateNextReset(Supplier<Long> clock, Long duration, TimeUnit unit) {
    Instant temporal = Instant.ofEpochMilli(clock.get());
    ZoneId zone = ZoneId.systemDefault();
    LocalDateTime time = LocalDateTime.ofInstant(temporal, zone);

    switch (unit) {
      case DAYS:
        LocalTime midnight = LocalTime.of(0, 0);
        time = time.plusDays(duration).with(midnight);
        break;
      case HOURS:
        time = time.plusHours(duration).withMinute(0).withSecond(0).withNano(0);
        break;
      case MINUTES:
        time = time.plusMinutes(duration.intValue()).withSecond(0).withNano(0);
        break;
      case SECONDS:
        time = time.plusSeconds(duration.intValue()).withNano(0);
        break;
    }

    return time.atZone(zone).toInstant().toEpochMilli();
  }

}
