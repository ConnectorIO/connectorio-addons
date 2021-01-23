package org.connectorio.chrono.shared;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.connectorio.chrono.Period;

public class FuturePeriodCalculator {

  private final Clock clock;
  private final Period period;

  public FuturePeriodCalculator(Clock clock, Period period) {
    this.clock = clock;
    this.period = period;
  }

  public ZonedDateTime calculate() {
    ZonedDateTime time = ZonedDateTime.ofInstant(clock.instant(), clock.getZone());
    LocalTime midnight = LocalTime.of(0, 0, 0, 0);

    switch (period) {
      case YEAR:
        time = time.plusYears(1).withDayOfYear(1).with(midnight);
        break;
      case MONTH:
        time = time.plusMonths(1).withDayOfMonth(1).with(midnight);
        break;
      case WEEK:
        time = time.plusWeeks(1).with(DayOfWeek.MONDAY).with(midnight);
        break;
      case DAY:
        time = time.plusDays(1).with(midnight);
        break;
      case HOUR:
        time = time.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        break;
      case HALF_HOUR:
        time = time.withMinute(0).withSecond(0).withNano(0).plusMinutes((time.getMinute() / 30) * 30 + 30);
        break;
      case QUARTER_HOUR:
        time = time.withMinute(0).withSecond(0).withNano(0).plusMinutes((time.getMinute() / 15) * 15 + 15);
        break;
      case MINUTE:
        time = time.plusMinutes(1).withSecond(0).withNano(0);
        break;
      case SECOND:
        time = time.plusSeconds(1).withNano(0);
        break;
    }

    return time;
  }

  public Period getPeriod() {
    return period;
  }

  public String toString() {
    return "Future Period Calculator [" + period + "]";
  }

}
