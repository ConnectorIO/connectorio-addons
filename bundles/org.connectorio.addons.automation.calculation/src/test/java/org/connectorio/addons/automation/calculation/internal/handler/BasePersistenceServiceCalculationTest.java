package org.connectorio.addons.automation.calculation.internal.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;

public class BasePersistenceServiceCalculationTest {

  public static final String PERSISTENCE_SERVICE_ID = "magic-persistence";
  public static final String ITEM_ENERGY_READING = "energyReading";
  public static final String ITEM_ENERGY_USE = "energyUse";
  protected PersistenceServiceCalculationActionHandler handler;
  @Mock
  protected PersistenceServiceRegistry persistenceServiceRegistry;
  @Mock
  protected EventPublisher eventPublisher;
  @Mock
  protected QueryablePersistenceService persistenceService;

  protected static ZonedDateTime createInstant(int year, int month, int day, int hour, int minute, int second) {
    return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, second, 0), ZoneOffset.UTC);
  }

  protected HistoricItem createMockItem(double value, ZonedDateTime timestamp, Map<ZonedDateTime, HistoricItem> readings) {
    HistoricItem item = mock(HistoricItem.class, Mockito.withSettings().lenient());
    when(item.getState()).thenReturn(new QuantityType<>(value, Units.KILOWATT_HOUR));
    when(item.getTimestamp()).thenReturn(timestamp);
    readings.put(timestamp, item);
    return item;
  }

  static class ReadingAnswer implements Answer<List<HistoricItem>> {

    private final Map<ZonedDateTime, HistoricItem> readings;

    ReadingAnswer(Map<ZonedDateTime, HistoricItem> readings) {
      this.readings = readings;
    }

    @Override
    public List<HistoricItem> answer(InvocationOnMock inv) throws Throwable {
      FilterCriteria argument = inv.getArgument(0, FilterCriteria.class);
      if (argument == null) {
        return Collections.emptyList();
      }

      ZonedDateTime dateTime = Optional.ofNullable(argument.getBeginDate()).orElse(argument.getEndDate());
      return Collections.singletonList(readings.get(dateTime));
    }
  }
}
