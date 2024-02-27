/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.persistence.memory.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.assertj.core.api.ListAssert;
import org.connectorio.addons.test.ItemMutation;
import org.connectorio.addons.test.StubItemBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;

@ExtendWith(MockitoExtension.class)
class MemoryPersistenceServiceTest {

  public static final String TEST_1 = "test1";
  public static final String TEST_2 = "test2";

  TimeZoneProvider tz = () -> ZoneOffset.UTC;

  Item item1 = StubItemBuilder.createNumber(TEST_1).build();
  Item item2 = StubItemBuilder.createNumber(TEST_2).build();

  @Test
  void testWriteAndQuery() {
    MemoryPersistenceService service = new MemoryPersistenceService(tz);
    new ItemMutation(item1).accept(new DecimalType(10));
    service.store(item1);

    assertThat(service.getItemInfo())
      .hasSize(1)
      .element(0).matches(info -> info.getCount() == 1);

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_1)))
      .hasSize(1);

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_2)))
      .isEmpty();
  }

  @Test
  void testWriteAndSortingQuery() {
    MemoryPersistenceService service = new MemoryPersistenceService(tz);
    new ItemMutation(item1).accept(new DecimalType(10));
    service.store(item1);
    new ItemMutation(item1).accept(new DecimalType(20));
    service.store(item1);

    assertThat(service.getItemInfo())
      .hasSize(1)
      .element(0).matches(info -> info.getCount() == 2);

    ListAssert<HistoricItem> itemAssert = assertThat(fetch(service, new FilterCriteria().setItemName(TEST_1).setOrdering(Ordering.DESCENDING)))
      .hasSize(2);
    itemAssert.element(0).matches(state -> state.getState().equals(new DecimalType(20)));
    itemAssert.element(1).matches(state -> state.getState().equals(new DecimalType(10)));

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_2)))
      .isEmpty();
  }

  @Test
  void testPaging() {
    MemoryPersistenceService service = new MemoryPersistenceService(tz);
    new ItemMutation(item1).accept(new DecimalType(10));
    service.store(item1);
    new ItemMutation(item1).accept(new DecimalType(20));
    service.store(item1);
    new ItemMutation(item1).accept(new DecimalType(30));
    service.store(item1);
    new ItemMutation(item1).accept(new DecimalType(40));
    service.store(item1);

    assertThat(service.getItemInfo())
      .hasSize(1)
      .element(0).matches(info -> info.getCount() == 4);

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_1).setPageSize(1)))
      .hasSize(1)
      .element(0).matches(state -> state.getState().equals(new DecimalType(40)));

    assertThat(
        fetch(service, new FilterCriteria().setItemName(TEST_1).setPageNumber(1).setPageSize(1)))
      .hasSize(1)
      .element(0).matches(state -> state.getState().equals(new DecimalType(30)));

    assertThat(
        fetch(service, new FilterCriteria().setItemName(TEST_1).setPageNumber(2).setPageSize(1)))
      .hasSize(1)
      .element(0).matches(state -> state.getState().equals(new DecimalType(20)));

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_1).setPageSize(3).setOrdering(Ordering.ASCENDING)))
      .hasSize(3)
      .extracting(HistoricItem::getState)
      .containsExactly(new DecimalType(10), new DecimalType(20), new DecimalType(30));

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_1).setPageNumber(1).setPageSize(2).setOrdering(Ordering.ASCENDING)))
      .hasSize(2)
      .extracting(HistoricItem::getState)
      .containsExactly(new DecimalType(30), new DecimalType(40));

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_1).setPageNumber(1).setPageSize(2)))
      .hasSize(2)
      .extracting(HistoricItem::getState)
      .containsExactly(new DecimalType(20), new DecimalType(10));
  }

  @Test
  void testWriteSameDate() {
    ZonedDateTime dateTime = createInstant(2024, 2, 22, 19, 7, 30);
    MemoryPersistenceService service = new MemoryPersistenceService(tz);
    service.store(item2, Date.from(dateTime.toInstant()), new DecimalType(10));
    service.store(item2, Date.from(dateTime.toInstant()), new DecimalType(20));

    assertThat(service.getItemInfo())
      .hasSize(1)
      .element(0).matches(info -> info.getCount() == 1);

    ListAssert<HistoricItem> itemAssert = assertThat(
        fetch(service, new FilterCriteria().setItemName(TEST_2).setOrdering(Ordering.DESCENDING)))
      .hasSize(1);
    itemAssert.element(0).matches(state -> state.getState().equals(new DecimalType(20)));

    assertThat(fetch(service, new FilterCriteria().setItemName(TEST_1)))
      .isEmpty();
  }

  @ParameterizedTest
  @MethodSource("source")
  void testDateFilters(
    @ConvertWith(DateConverter.class) ZonedDateTime begin,
    @ConvertWith(DateConverter.class) ZonedDateTime end,
    @ConvertWith(DateConverter.class) ZonedDateTime firstTimestamp,
    @ConvertWith(DateConverter.class) ZonedDateTime secondTimestamp,
    Integer firstValue, Integer secondValue
  ) {
    MemoryPersistenceService service = new MemoryPersistenceService(tz);
    service.store(item2, Date.from(firstTimestamp.toInstant()), new DecimalType(10));
    service.store(item2, Date.from(secondTimestamp.toInstant()), new DecimalType(20));

    assertThat(service.getItemInfo())
      .hasSize(1)
      .element(0).matches(info -> info.getCount() == 2);

    FilterCriteria criteria = new FilterCriteria().setItemName(TEST_2).setOrdering(Ordering.ASCENDING);
    if (begin != null) {
      criteria.setBeginDate(begin);
    }
    if (end != null) {
      criteria.setEndDate(end);
    }

    int size = (firstValue == null ? 0 : 1) + (secondValue == null ? 0 : 1);

    ListAssert<HistoricItem> itemAssert = assertThat(fetch(service, criteria))
      .hasSize(size);
    if (firstValue != null) {
      itemAssert.element(0).matches(state -> state.getState().equals(new DecimalType(firstValue)))
        .describedAs("First value should match %d", firstValue);
    }
    if (secondValue != null) {
      itemAssert.element(1).matches(state -> state.getState().equals(new DecimalType(secondValue)))
        .describedAs("Second value should match %d", secondValue);
    }
  }

  public static Stream<Arguments> source() {
    return Stream.of(
      Arguments.of("2024.02.22 18:59:59.999Z", "2024.02.22 19:15:00.000Z", "2024.02.22 18:59:59.999Z", "2024.02.22 19:15:00.000Z", 10, 20),
      Arguments.of("2024.02.22 18:50:59.999Z", "2024.02.22 19:25:00.000Z", "2024.02.22 18:59:59.999Z", "2024.02.22 19:15:00.000Z", 10, 20),
      Arguments.of("2024.02.22 18:59:59.999Z", "2024.02.22 19:15:00.000Z", "2024.02.22 18:50:59.999Z", "2024.02.22 19:25:00.000Z", null, null)
    );
  }

  public static ZonedDateTime createInstant(int year, int month, int day, int hour, int minute, int second) {
    return createInstant(year, month, day, hour, minute, second, 0);
  }

  public static ZonedDateTime createInstant(int year, int month, int day, int hour, int minute, int second, int nanos) {
    return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, second, nanos), ZoneOffset.UTC);
  }

  public List<HistoricItem> fetch(MemoryPersistenceService service, FilterCriteria criteria) {
    Iterable<HistoricItem> results = service.query(criteria);
    if (results instanceof List) {
      return (List<HistoricItem>) results;
    }
    return StreamSupport.stream(results.spliterator(), false).collect(Collectors.toList());
  }

  static class DateConverter implements ArgumentConverter {

    private final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS'Z'");

    @Override
    public ZonedDateTime convert(Object source, ParameterContext context) throws ArgumentConversionException {
      if (source instanceof String) {
        return LocalDateTime.parse((CharSequence) source, FORMAT).atZone(ZoneOffset.UTC);
      }
      // non-convertible
      return null;
    }
  }

}
