package org.connectorio.addons.test.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.connectorio.addons.test.persistence.Entry.entry;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;

class QueryableStubPersistenceServiceTest {

  ZonedDateTime START = ZonedDateTime.of(LocalDateTime.of(
      LocalDate.of(2021, 12, 18),
      LocalTime.of(22, 0)
    ), ZoneId.systemDefault()
  );

  String ITEM = "test";

  QueryableStubPersistenceService service = new QueryableStubPersistenceService("test",
    entry(START, "another", new DecimalType(10)),
    entry(START, ITEM, new DecimalType(10)),
    entry(START.plusDays(1), ITEM, new DecimalType(100)),
    entry(START.plusDays(2), ITEM, new DecimalType(100))
  );

  @Test
  void verifyPaging() {
    int page = 0;

    List<HistoricItem> items;
    do {
      items = Streams.stream(service.query(new FilterCriteria()
        .setPageSize(1)
        .setPageNumber(page++))
      ).collect(Collectors.toList());
      assertThat(items).hasSize(1);
    } while (page < 4);

    items = Streams.stream(service.query(new FilterCriteria()
      .setPageSize(1)
      .setPageNumber(page++))
    ).collect(Collectors.toList());

    assertThat(items).isEmpty();
  }

  @Test
  void verifyNoFiltering() {
    List<HistoricItem> items = Streams.stream(service.query(new FilterCriteria()))
      .collect(Collectors.toList());

    assertThat(items).hasSize(4);
  }

  @Test
  void verifyItemFiltering() {
    List<HistoricItem> items = Streams.stream(service.query(new FilterCriteria()
      .setItemName(ITEM))
    ).collect(Collectors.toList());

    assertThat(items).hasSize(3);
  }

  @Test
  void verifyItemAndBeginDateFiltering() {
    List<HistoricItem> items = Streams.stream(service.query(new FilterCriteria()
      .setItemName(ITEM)
      .setBeginDate(START.plusMinutes(1)))
    ).collect(Collectors.toList());

    assertThat(items).hasSize(2);
  }

  @Test
  void verifyItemAndEndDateFiltering() {
    List<HistoricItem> items = Streams.stream(service.query(new FilterCriteria()
      .setItemName(ITEM)
      .setEndDate(START.plusDays(2).minusMinutes(1)))
    ).collect(Collectors.toList());

    assertThat(items).hasSize(2);
  }

  @Test
  void verifyItemStartAndEndDateFiltering() {
    List<HistoricItem> items = Streams.stream(service.query(new FilterCriteria()
      .setItemName(ITEM)
      .setBeginDate(START.plusMinutes(1))
      .setEndDate(START.plusDays(2).minusMinutes(1)))
    ).collect(Collectors.toList());

    assertThat(items).hasSize(1);
  }

}