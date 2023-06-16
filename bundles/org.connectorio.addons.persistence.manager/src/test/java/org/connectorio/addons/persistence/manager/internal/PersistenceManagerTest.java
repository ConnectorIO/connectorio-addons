/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.persistence.manager.internal;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.internal.common.SafeCallerImpl;
import org.openhab.core.internal.scheduler.CronSchedulerImpl;
import org.openhab.core.internal.scheduler.SchedulerImpl;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.PersistenceItemConfiguration;
import org.openhab.core.persistence.PersistenceManager;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.config.PersistenceItemConfig;
import org.openhab.core.persistence.internal.PersistenceManagerImpl;
import org.openhab.core.persistence.registry.PersistenceServiceConfiguration;
import org.openhab.core.persistence.registry.PersistenceServiceConfigurationRegistry;
import org.openhab.core.persistence.strategy.PersistenceStrategy.Globals;
import org.openhab.core.scheduler.Scheduler;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.StartLevelService;

@ExtendWith(MockitoExtension.class)
public class PersistenceManagerTest {

  static String INFLUX = "influxdb";

  @Mock
  ItemRegistry itemRegistry;

  @Mock
  ReadyService readyService;

  @Mock
  PersistenceService persistenceService;

  @Mock
  PersistenceServiceConfigurationRegistry configurationRegistry;

  private AccessiblePersistenceManager manager;

  Item OUTSIDE_TEMPERATURE_ITEM = new NumberItem("Number", "OutsideTemperature", null);
  Item INSIDE_TEMPERATURE_ITEM = new NumberItem("Number", "InsideTemperature", null);
  Item ENERGY_ITEM = new NumberItem("Number", "ElectricityConsumption", null);
  Item TEMPERATURES = new GroupItem("Temperatures") {
    {{
      addMember(OUTSIDE_TEMPERATURE_ITEM);
      addMember(INSIDE_TEMPERATURE_ITEM);
    }}
  };

  @BeforeEach
  void setup() throws Exception {
    SchedulerImpl scheduler = new SchedulerImpl();
    manager = new AccessiblePersistenceManager(
        new CronSchedulerImpl(scheduler), scheduler, itemRegistry, new SafeCallerImpl(Collections.emptyMap()),
        readyService, configurationRegistry
    );

    when(persistenceService.getId()).thenReturn(INFLUX);
    when(persistenceService.getDefaultStrategies()).thenReturn(Collections.singletonList(Globals.CHANGE));

    // expected registry lookups
    when(itemRegistry.getItems()).thenReturn(Arrays.asList(OUTSIDE_TEMPERATURE_ITEM, ENERGY_ITEM, INSIDE_TEMPERATURE_ITEM, TEMPERATURES));

//  registerItem(OUTSIDE_TEMPERATURE_ITEM);
//  registerItem(INSIDE_TEMPERATURE_ITEM);
//  registerItem(ENERGY_ITEM);
//  registerItem(TEMPERATURES);

    manager.addPersistenceService(persistenceService);
  }

  private void registerItem(Item inside_temperature_item) throws ItemNotFoundException {
    when(itemRegistry.getItem(inside_temperature_item.getName())).thenReturn(inside_temperature_item);
  }

  @Test
  void checkManagerWithServiceDefaults() throws Exception {
    manager.onReadyMarkerAdded(new ReadyMarker(StartLevelService.STARTLEVEL_MARKER_TYPE, Integer.toString(StartLevelService.STARTLEVEL_MODEL)));

    Thread.sleep(1000);

    DecimalType temp = new DecimalType(20.1);
    DecimalType old = new DecimalType(20.2);
    manager.stateChanged(OUTSIDE_TEMPERATURE_ITEM, old, temp);

    verify(persistenceService, times(1)).store(OUTSIDE_TEMPERATURE_ITEM, null);
  }

  @Test
  void checkManagerWithEmptyItemSettings() throws Exception {
    manager.added(new PersistenceServiceConfiguration(INFLUX,
        Arrays.asList(new PersistenceItemConfiguration(
          Arrays.asList(new PersistenceItemConfig(OUTSIDE_TEMPERATURE_ITEM.getName())),
          null, Collections.emptyList()
        )
      ),
      Collections.emptyMap(),
      Arrays.asList(),
      Arrays.asList(),
      Arrays.asList()
    ));
    manager.onReadyMarkerAdded(new ReadyMarker(StartLevelService.STARTLEVEL_MARKER_TYPE, Integer.toString(StartLevelService.STARTLEVEL_MODEL)));

    Thread.sleep(1000);

    DecimalType temp = new DecimalType(20.1);
    DecimalType old = new DecimalType(20.2);
    manager.stateChanged(OUTSIDE_TEMPERATURE_ITEM, old, temp);

    verify(persistenceService, never()).store(OUTSIDE_TEMPERATURE_ITEM, null);
  }

  @Test
  void checkManagerWithVerboseItemSettings() throws Exception {
    manager.added(new PersistenceServiceConfiguration(INFLUX,
        Arrays.asList(new PersistenceItemConfiguration(
          Arrays.asList(new PersistenceItemConfig(OUTSIDE_TEMPERATURE_ITEM.getName())),
          Arrays.asList(Globals.UPDATE), Collections.emptyList()
        ), new PersistenceItemConfiguration(
          Arrays.asList(new PersistenceItemConfig(OUTSIDE_TEMPERATURE_ITEM.getName())),
          Arrays.asList(Globals.CHANGE), Collections.emptyList()
        )
      ),
      Collections.emptyMap(),
      Arrays.asList(),
      Arrays.asList(),
      Arrays.asList()
    ));
    manager.onReadyMarkerAdded(new ReadyMarker(StartLevelService.STARTLEVEL_MARKER_TYPE, Integer.toString(StartLevelService.STARTLEVEL_MODEL)));

    Thread.sleep(1000);

    DecimalType temp = new DecimalType(20.1);
    DecimalType old = new DecimalType(20.2);
    manager.stateChanged(OUTSIDE_TEMPERATURE_ITEM, old, temp);
    manager.stateUpdated(OUTSIDE_TEMPERATURE_ITEM, temp);
    manager.stateUpdated(INSIDE_TEMPERATURE_ITEM, temp);

    verify(persistenceService, times(2)).store(OUTSIDE_TEMPERATURE_ITEM, null);
    verify(persistenceService, never()).store(eq(INSIDE_TEMPERATURE_ITEM), any());
    reset(persistenceService);

    manager.added(new PersistenceServiceConfiguration(INFLUX,
        Arrays.asList(new PersistenceItemConfiguration(
          Arrays.asList(new PersistenceItemConfig(OUTSIDE_TEMPERATURE_ITEM.getName())),
          Arrays.asList(Globals.UPDATE), Collections.emptyList()
        )
      ),
      Map.of(OUTSIDE_TEMPERATURE_ITEM.getName(), "rest"),
      Arrays.asList(),
      Arrays.asList(),
      Arrays.asList()
    ));

    manager.stateUpdated(INSIDE_TEMPERATURE_ITEM, temp);
    manager.stateUpdated(OUTSIDE_TEMPERATURE_ITEM, temp);
    verify(persistenceService, times(1)).store(OUTSIDE_TEMPERATURE_ITEM, "rest");
    verify(persistenceService, never()).store(eq(INSIDE_TEMPERATURE_ITEM), any());

  }


}
