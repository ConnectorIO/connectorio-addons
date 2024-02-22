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
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import org.assertj.core.api.IterableAssert;
import org.connectorio.addons.test.ItemMutation;
import org.connectorio.addons.test.StubItemBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
  @Mock
  TimeZoneProvider tz;

  Item item1 = StubItemBuilder.createNumber(TEST_1).build();
  Item item2 = StubItemBuilder.createNumber(TEST_2).build();

  @BeforeEach
  void setup() {
    when(tz.getTimeZone()).thenReturn(ZoneId.of("GMT"));
  }

  @Test
  void testWriteAndQuery() {
    MemoryPersistenceService service = new MemoryPersistenceService(tz);
    new ItemMutation(item1).accept(new DecimalType(10));
    service.store(item1);

    assertThat(service.getItemInfo())
      .hasSize(1)
      .element(0).matches(info -> info.getCount() == 1);

    assertThat(service.query(new FilterCriteria().setItemName(TEST_1)))
      .hasSize(1);

    assertThat(service.query(new FilterCriteria().setItemName(TEST_2)))
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

    IterableAssert<HistoricItem> itemAssert = assertThat(service.query(new FilterCriteria().setItemName(TEST_1).setOrdering(Ordering.DESCENDING)))
      .hasSize(2);
    itemAssert.element(0).matches(state -> state.getState().equals(new DecimalType(20)));
    itemAssert.element(1).matches(state -> state.getState().equals(new DecimalType(10)));

    assertThat(service.query(new FilterCriteria().setItemName(TEST_2)))
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

    assertThat(service.query(new FilterCriteria().setItemName(TEST_1).setPageSize(1)))
      .hasSize(1)
      .element(0).matches(state -> state.getState().equals(new DecimalType(40)));

    assertThat(service.query(new FilterCriteria().setItemName(TEST_1).setPageNumber(1).setPageSize(1)))
      .hasSize(1)
      .element(0).matches(state -> state.getState().equals(new DecimalType(30)));

    assertThat(service.query(new FilterCriteria().setItemName(TEST_1).setPageNumber(2).setPageSize(1)))
      .hasSize(1)
      .element(0).matches(state -> state.getState().equals(new DecimalType(20)));

    assertThat(service.query(new FilterCriteria().setItemName(TEST_1).setPageSize(3).setOrdering(Ordering.ASCENDING)))
      .hasSize(3)
      .extracting(HistoricItem::getState)
      .containsExactly(new DecimalType(10), new DecimalType(20), new DecimalType(30));

    assertThat(service.query(new FilterCriteria().setItemName(TEST_1).setPageNumber(1).setPageSize(2).setOrdering(Ordering.ASCENDING)))
      .hasSize(2)
      .extracting(HistoricItem::getState)
      .containsExactly(new DecimalType(30), new DecimalType(40));

    assertThat(service.query(new FilterCriteria().setItemName(TEST_1).setPageNumber(1).setPageSize(2)))
      .hasSize(2)
      .extracting(HistoricItem::getState)
      .containsExactly(new DecimalType(20), new DecimalType(10));
  }

}