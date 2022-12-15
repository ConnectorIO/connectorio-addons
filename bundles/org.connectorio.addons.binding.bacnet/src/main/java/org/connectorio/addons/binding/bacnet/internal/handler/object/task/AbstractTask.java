/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.handler.object.task;

import com.serotonin.bacnet4j.enums.DayOfWeek;
import com.serotonin.bacnet4j.enums.Month;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.CalendarEntry;
import com.serotonin.bacnet4j.type.constructed.DailySchedule;
import com.serotonin.bacnet4j.type.constructed.DateRange;
import com.serotonin.bacnet4j.type.constructed.DeviceObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.SpecialEvent;
import com.serotonin.bacnet4j.type.constructed.TimeValue;
import com.serotonin.bacnet4j.type.constructed.WeekNDay;
import com.serotonin.bacnet4j.type.constructed.WeekNDay.WeekOfMonth;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.Polarity;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.Date;
import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.SignedInteger;
import com.serotonin.bacnet4j.type.primitive.Time;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.code_house.bacnet4j.wrapper.api.BacNetToJavaConverter;
import org.connectorio.addons.temporal.calendar.CalendarEntryType;
import org.connectorio.addons.temporal.calendar.CalendarType;
import org.connectorio.addons.temporal.calendar.DateCalendarEntryType;
import org.connectorio.addons.temporal.DayOfWeekType;
import org.connectorio.addons.temporal.DayScheduleType;
import org.connectorio.addons.temporal.LocalDateRangeType;
import org.connectorio.addons.temporal.LocalDateType;
import org.connectorio.addons.temporal.LocalTimeType;
import org.connectorio.addons.temporal.month.BasicMonthType;
import org.connectorio.addons.temporal.month.ExtendedMonthType;
import org.connectorio.addons.temporal.month.MonthType;
import org.connectorio.addons.temporal.calendar.RangeCalendarEntryType;
import org.connectorio.addons.temporal.calendar.CompositeCalendarEntryType;
import org.connectorio.addons.temporal.WeekInMonthType;
import org.connectorio.addons.temporal.WeeklyScheduleType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTask implements Runnable, BacNetToJavaConverter<State> {

  private final Logger logger = LoggerFactory.getLogger(AbstractTask.class);

  @Override
  public State fromBacNet(Encodable encodable) {
    if (encodable instanceof Null) {
      return UnDefType.NULL;
    } else if (encodable instanceof Real) {
      return new DecimalType(((Real) encodable).floatValue());
    } else if (encodable instanceof BinaryPV) {
      return BinaryPV.active.equals(encodable) ? OnOffType.ON : OnOffType.OFF;
    } else if (encodable instanceof Polarity) {
      return Polarity.normal.equals(encodable) ? OnOffType.ON : OnOffType.OFF;
    } else if (encodable instanceof UnsignedInteger) {
      return new DecimalType(((UnsignedInteger) encodable).intValue());
    } else if (encodable instanceof SignedInteger) {
      return new DecimalType(((SignedInteger) encodable).intValue());
    } else if (encodable instanceof Boolean) {
      return Boolean.TRUE == encodable ? OnOffType.ON : OnOffType.OFF;
    } else if (encodable instanceof Time) {
      Time time = (Time) encodable;
      LocalTime localTime = LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), time.getHundredth());
      return new LocalTimeType(localTime);
    } else if (encodable instanceof Date) {
      Date date = (Date) encodable;
      return new LocalDateType(date.calculateGC().toZonedDateTime());
    } else if (encodable instanceof Enumerated) {
      return new DecimalType(((Enumerated) encodable).intValue());
    } else if (encodable instanceof SequenceOf) {
      SequenceOf<?> sequence = (SequenceOf<?>) encodable;
      if (sequence.getCount() > 0) {
        Encodable value = sequence.get(0);
        if (value instanceof DailySchedule) {
          WeeklyScheduleType schedule = new WeeklyScheduleType();
          schedule = convertDay(value, schedule, schedule::withMondaySchedule);
          schedule = convertDay(sequence.get(1), schedule, schedule::withTuesdaySchedule);
          schedule = convertDay(sequence.get(2), schedule, schedule::withWednesdaySchedule);
          schedule = convertDay(sequence.get(3), schedule, schedule::withThursdaySchedule);
          schedule = convertDay(sequence.get(4), schedule, schedule::withFridaySchedule);
          schedule = convertDay(sequence.get(5), schedule, schedule::withSaturdaySchedule);
          schedule = convertDay(sequence.get(6), schedule, schedule::withSundaySchedule);
          return schedule;
        } else if (value instanceof DeviceObjectPropertyReference) {
          List<String> references = new ArrayList<>();
          for (DeviceObjectPropertyReference ref : ((SequenceOf<DeviceObjectPropertyReference>) sequence)) {
            references.add(ref.getDeviceIdentifier() + "." + ref.getObjectIdentifier().getObjectType().toString() + "." + ref.getObjectIdentifier().getInstanceNumber() + "." + ref.getPropertyIdentifier());
          }

          return new StringListType(references);
        } else if (value instanceof SpecialEvent) {
          SpecialEvent event = (SpecialEvent) value;

          List<CalendarEntryType> events = new ArrayList<>();
          State state = fromBacNet(event);

          return new CalendarType(events);
        }
      }
    } else if (encodable instanceof DailySchedule) {
      DailySchedule dailySchedule = (DailySchedule) encodable;
      Map<LocalTimeType, State> schedule = new LinkedHashMap<>();
      for (TimeValue tv : dailySchedule.getDaySchedule()) {
        schedule.put((LocalTimeType) fromBacNet(tv.getTime()), fromBacNet(tv.getValue()));
      }
      return new DayScheduleType(schedule);
    } else if (encodable instanceof DateRange) {
      DateRange range = (DateRange) encodable;
      return new LocalDateRangeType(
        new LocalDateType(range.getStartDate().calculateGC().toZonedDateTime().toLocalDate()),
        new LocalDateType(range.getEndDate().calculateGC().toZonedDateTime().toLocalDate())
      );
    } else if (encodable instanceof SpecialEvent) {
      SpecialEvent event = (SpecialEvent) encodable;
      CalendarEntry entry = event.getCalendarEntry();
      if (entry.isDate()) {
        LocalDateType localDate = (LocalDateType) fromBacNet(entry.getDate());
        return new DateCalendarEntryType(localDate);
      } else if (entry.isDateRange()) {
        LocalDateRangeType dateRange = (LocalDateRangeType) fromBacNet(entry.getDateRange());
        return new RangeCalendarEntryType(dateRange);
      } else if (entry.isWeekNDay()) {
        WeekNDay weekNDay = entry.getWeekNDay();
        MonthType month = convertMonth(weekNDay.getMonth());
        WeekInMonthType week = convertWeek(weekNDay.getWeekOfMonth());
        DayOfWeekType day = convertDayOfWeek(weekNDay);
        return new CompositeCalendarEntryType(month, week, day);
      }
    }

    logger.info("Received property value is currently not supported");
    return UnDefType.UNDEF;
  }

  private MonthType convertMonth(Month month) {
    switch (month) {
      case JANUARY:
        return BasicMonthType.JANUARY;
      case FEBRUARY:
        return BasicMonthType.FEBRUARY;
      case MARCH:
        return BasicMonthType.MARCH;
      case APRIL:
        return BasicMonthType.APRIL;
      case MAY:
        return BasicMonthType.MAY;
      case JUNE:
        return BasicMonthType.JUNE;
      case JULY:
        return BasicMonthType.JULY;
      case AUGUST:
        return BasicMonthType.AUGUST;
      case SEPTEMBER:
        return BasicMonthType.SEPTEMBER;
      case OCTOBER:
        return BasicMonthType.OCTOBER;
      case NOVEMBER:
        return BasicMonthType.NOVEMBER;
      case DECEMBER:
        return BasicMonthType.DECEMBER;

      case ODD_MONTHS:
        return ExtendedMonthType.ODD_MONTHS;
      case EVEN_MONTHS:
        return ExtendedMonthType.EVEN_MONTHS;
      case UNSPECIFIED:
        return ExtendedMonthType.ANY;
    }
    return null;
  }

  private WeekInMonthType convertWeek(WeekOfMonth weekOfMonth) {
    if (weekOfMonth.equals(WeekOfMonth.days1to7)) {
      return WeekInMonthType.FIRST;
    }
    if (weekOfMonth.equals(WeekOfMonth.days8to14)) {
      return WeekInMonthType.SECOND;
    }
    if (weekOfMonth.equals(WeekOfMonth.days15to21)) {
      return WeekInMonthType.THIRD;
    }
    if (weekOfMonth.equals(WeekOfMonth.days22to28)) {
      return WeekInMonthType.FOURTH;
    }
    if (weekOfMonth.equals(WeekOfMonth.days29to31)) {
      return WeekInMonthType.FIFTH;
    }
    if (weekOfMonth.equals(WeekOfMonth.last7Days)) {
      return WeekInMonthType.LAST_7_DAYS;
    }
    return null;
  }

  private static DayOfWeekType convertDayOfWeek(WeekNDay weekNDay) {
    DayOfWeek dayOfWeek = weekNDay.getDayOfWeek();
    switch (dayOfWeek) {
      case MONDAY:
        return DayOfWeekType.MONDAY;
      case TUESDAY:
        return DayOfWeekType.TUESDAY;
      case WEDNESDAY:
        return DayOfWeekType.WEDNESDAY;
      case THURSDAY:
        return DayOfWeekType.THURSDAY;
      case FRIDAY:
        return DayOfWeekType.FRIDAY;
      case SATURDAY:
        return DayOfWeekType.SATURDAY;
      case SUNDAY:
        return DayOfWeekType.SUNDAY;
      default:
        //return UnDefType.UNDEF;
    }
    return null;
  }

  private WeeklyScheduleType convertDay(Encodable encodable, WeeklyScheduleType fallback, Function<DayScheduleType, WeeklyScheduleType> function) {
    State state = fromBacNet(encodable);
    if (state instanceof DayScheduleType) {
      return function.apply((DayScheduleType) state);
    }
    return fallback;
  }

}
