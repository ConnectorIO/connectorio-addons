/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.profile.counter.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import org.connectorio.addons.profile.counter.internal.BaseCounterProfile.UninitializedBehavior;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;

/**
 * Basic test which confirms conversion logic for quantity/no quantity reading.
 */
@ExtendWith(MockitoExtension.class)
class LimitCounterTopProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;
  @Mock
  LinkedItemStateRetriever itemStateRetriever;

  @Test
  void checkDecimalValueFromItem() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("anomaly", "10");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    // default -> first state from item!
    LimitCounterTopProfile profile = new LimitCounterTopProfile(callback, context, itemStateRetriever);

    // update from handler before item state is set -> no interactions
    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    Mockito.verifyNoInteractions(callback);

    // item initializes last state, then handler can send its updates
    profile.onStateUpdateFromItem(new DecimalType(10.0));
    Mockito.verify(callback).handleCommand(new DecimalType(10.0));
    profile.onStateUpdateFromHandler(new DecimalType(10.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(10.1));

    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    Mockito.verifyNoMoreInteractions(callback);

    profile.onStateUpdateFromHandler(new DecimalType(11.0));
    Mockito.verify(callback).sendUpdate(new DecimalType(11.0));
  }

  @Test
  void checkDecimalValueFromHandler() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("anomaly", "10");
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_HANDLER.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    // default -> first state from item!
    LimitCounterTopProfile profile = new LimitCounterTopProfile(callback, context, itemStateRetriever);

    // update from handler before item state is set -> no interactions
    profile.onStateUpdateFromItem(new DecimalType(13.0));
    Mockito.verifyNoInteractions(callback);

    // item initializes last state, then handler can send its updates
    profile.onStateUpdateFromHandler(new DecimalType(10.0));
    Mockito.verify(callback).sendUpdate(new DecimalType(10.0));
    profile.onStateUpdateFromHandler(new DecimalType(10.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(10.1));
  }

  @Test
  void checkDecimalValueFromPersistence() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("anomaly", "10"); // max change is 10%
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);
    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(new DecimalType(10.0));

    // we shall start with 10.0 retrieved from persistence, so we should only accept values below 11
    LimitCounterTopProfile profile = new LimitCounterTopProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromItem(new DecimalType(13.0));
    Mockito.verifyNoInteractions(callback);

    // anomaly call
    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    Mockito.verifyNoInteractions(callback);
    // accepted call
    profile.onStateUpdateFromHandler(new DecimalType(10.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(10.1));
  }

  @Test
  void checkQuantityValueFromItem() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("anomaly", "10");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    // default -> first state from item!
    LimitCounterTopProfile profile = new LimitCounterTopProfile(callback, context, itemStateRetriever);

    // update from handler before item state is set -> no interactions
    profile.onStateUpdateFromHandler(new QuantityType<>(13.0, Units.WATT_HOUR));
    Mockito.verifyNoInteractions(callback);

    // item initializes last state, then handler can send its updates
    profile.onStateUpdateFromItem(new QuantityType<>(10.0, Units.WATT_HOUR));
    Mockito.verify(callback).handleCommand(new QuantityType<>(10.0, Units.WATT_HOUR));
    profile.onStateUpdateFromHandler(new QuantityType<>(10.1, Units.WATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(10.1, Units.WATT_HOUR));

    profile.onStateUpdateFromHandler(new QuantityType<>(13.0, Units.WATT_HOUR));
    Mockito.verifyNoMoreInteractions(callback);

    Unit<Energy> milliWatHour = MetricPrefix.MILLI(Units.WATT_HOUR);
    profile.onStateUpdateFromHandler(new QuantityType<>(11000, milliWatHour));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(11000, milliWatHour));
  }

}