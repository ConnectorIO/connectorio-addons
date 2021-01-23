package org.connectorio.chrono.shared;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.connectorio.chrono.Period;

public class PastPeriodCalculator {

  private final Clock clock;
  private final Period period;

  public PastPeriodCalculator(Clock clock, Period period) {
    this.clock = clock;
    this.period = period;
  }

  public ZonedDateTime calculate() {
    ZonedDateTime time = ZonedDateTime.ofInstant(clock.instant(), clock.getZone());
    LocalTime midnight = LocalTime.of(0, 0, 0, 0);

    switch (period) {
      case YEAR:
        time = time.withDayOfYear(1).with(midnight);
        break;
      case MONTH:
        time = time.withDayOfMonth(1).with(midnight);
        break;
      case WEEK:
        time = time.with(DayOfWeek.MONDAY).with(midnight);
        break;
      case DAY:
        time = time.with(midnight);
        break;
      case HOUR:
        time = time.withMinute(0).withSecond(0).withNano(0);
        break;
      case HALF_HOUR:
        time = time.withMinute(0).withSecond(0).withNano(0).plusMinutes((time.getMinute() / 30) * 30);
        break;
      case QUARTER_HOUR:
        time = time.withMinute(0).withSecond(0).withNano(0).plusMinutes((time.getMinute() / 15) * 15);
        break;
      case MINUTE:
        time = time.withSecond(0).withNano(0);
        break;
      case SECOND:
        time = time.withNano(0);
        break;
    }

    return time;
  }

  public Period getPeriod() {
    return period;
  }

  public String toString() {
    return "Past Period Calculator [" + period + "]";
  }

}
