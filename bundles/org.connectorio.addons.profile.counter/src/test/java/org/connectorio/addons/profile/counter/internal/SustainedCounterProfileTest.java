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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.HashMap;
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
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;

/**
 * Basic test which confirms conversion logic for sustained counter.
 */
@ExtendWith(MockitoExtension.class)
class SustainedCounterProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;
  @Mock
  LinkedItemStateRetriever itemStateRetriever;

  @Test
  void checkDecimalWithNoPersistentState() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(null);

    // we shall start with 10.0 retrieved from persistence
    SustainedCounterProfile profile = new SustainedCounterProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    profile.onStateUpdateFromHandler(new DecimalType(13.1));

    // reset call
    profile.onStateUpdateFromHandler(new DecimalType(10));

    Mockito.verify(callback).sendUpdate(new DecimalType(13));
    Mockito.verify(callback).sendUpdate(new DecimalType(13.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(23.1));
  }

  @Test
  void checkQuantityWithNoPersistentState() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(null);

    // we shall start with 10.0 retrieved from persistence
    SustainedCounterProfile profile = new SustainedCounterProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new QuantityType<>(13.0, Units.KILOWATT_HOUR));
    profile.onStateUpdateFromHandler(new QuantityType<>(13.1, Units.KILOWATT_HOUR));

    // reset call
    profile.onStateUpdateFromHandler(new QuantityType<>(10, Units.KILOWATT_HOUR));

    Mockito.verify(callback).sendUpdate(new QuantityType(13, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType(13.1, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(23.1, Units.KILOWATT_HOUR));
  }

  @Test
  void checkDecimalValueFromPersistence() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(new DecimalType(10));

    // we shall start with 10.0 retrieved from persistence
    SustainedCounterProfile profile = new SustainedCounterProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    profile.onStateUpdateFromHandler(new DecimalType(13.1));

    // reset call
    profile.onStateUpdateFromHandler(new DecimalType(10));

    Mockito.verify(callback).sendUpdate(new DecimalType(23));
    Mockito.verify(callback).sendUpdate(new DecimalType(23.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(33.1));
  }

  @Test
  void checkQuantityValueFromPersistence() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(new QuantityType<>(10, Units.KILOWATT_HOUR));

    // we shall start with 10.0 retrieved from persistence
    SustainedCounterProfile profile = new SustainedCounterProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new QuantityType<>(13.0, Units.KILOWATT_HOUR));
    profile.onStateUpdateFromHandler(new QuantityType<>(13.1, Units.KILOWATT_HOUR));

    // reset call
    profile.onStateUpdateFromHandler(new QuantityType<>(10, Units.KILOWATT_HOUR));

    Mockito.verify(callback).sendUpdate(new QuantityType(23, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType(23.1, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(33.1, Units.KILOWATT_HOUR));
  }

  @Test
  void checkSequentialUpdateDecimalValueFromPersistence() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(new DecimalType(0));

    // we shall start with 10.0 retrieved from persistence
    SustainedCounterProfile profile = new SustainedCounterProfile(callback, context, itemStateRetriever);

    int index = 0;
    while (index++ < 100) {
      // update from item above accepted level
      profile.onStateUpdateFromHandler(new DecimalType(index));
    }
    Mockito.verify(callback).sendUpdate(new DecimalType(100));

    // reset call
    profile.onStateUpdateFromHandler(new DecimalType(10));
    Mockito.verify(callback).sendUpdate(new DecimalType(110));
  }
}