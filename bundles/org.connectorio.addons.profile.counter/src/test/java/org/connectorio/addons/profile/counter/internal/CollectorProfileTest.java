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
package org.connectorio.addons.profile.counter.internal;

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
 * Basic test which confirms conversion logic for collector profile.
 */
@ExtendWith(MockitoExtension.class)
class CollectorProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;
  @Mock
  LinkedItemStateRetriever itemStateRetriever;

  @Test
  void checkDecimalWithPersistentStateAndIncomingUpdate() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(new DecimalType(1000));
    when(context.getConfiguration()).thenReturn(config);

    CollectorProfile profile = new CollectorProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new DecimalType(10.0));
    profile.onStateUpdateFromHandler(new DecimalType(13.1));
    profile.onStateUpdateFromHandler(new DecimalType(10));

    // verify results
    Mockito.verify(callback).sendUpdate(new DecimalType(1010));
    Mockito.verify(callback).sendUpdate(new DecimalType(1023.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(1033.1));
  }

  @Test
  void checkDecimalWithNoPersistentState() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(null);

    CollectorProfile profile = new CollectorProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    profile.onStateUpdateFromHandler(new DecimalType(13.1));
    profile.onStateUpdateFromHandler(new DecimalType(10));

    Mockito.verify(callback).sendUpdate(new DecimalType(13));
    Mockito.verify(callback).sendUpdate(new DecimalType(26.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(36.1));
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
    CollectorProfile profile = new CollectorProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new QuantityType<>(13.0, Units.KILOWATT_HOUR));
    profile.onStateUpdateFromHandler(new QuantityType<>(13.1, Units.KILOWATT_HOUR));
    profile.onStateUpdateFromHandler(new QuantityType<>(10, Units.KILOWATT_HOUR));

    Mockito.verify(callback).sendUpdate(new QuantityType<>(13, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(26.1, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(36.1, Units.KILOWATT_HOUR));
  }

  @Test
  void checkDecimalValueFromPersistence() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(new DecimalType(1000));

    // we shall start with 10.0 retrieved from persistence
    CollectorProfile profile = new CollectorProfile(callback, context, itemStateRetriever);

    // update from item above accepted level
    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    profile.onStateUpdateFromHandler(new DecimalType(13.1));
    profile.onStateUpdateFromHandler(new DecimalType(10));

    Mockito.verify(callback).sendUpdate(new DecimalType(1013));
    Mockito.verify(callback).sendUpdate(new DecimalType(1026.1));
    Mockito.verify(callback).sendUpdate(new DecimalType(1036.1));
  }

  @Test
  void checkQuantityValueFromPersistence() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("uninitializedBehavior", UninitializedBehavior.RESTORE_FROM_PERSISTENCE.name());
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(itemStateRetriever.getItemName(callback)).thenReturn("foo");
    when(itemStateRetriever.retrieveState("foo")).thenReturn(new QuantityType<>(1000, Units.KILOWATT_HOUR));

    // we shall start with 10.0 retrieved from persistence
    CollectorProfile profile = new CollectorProfile(callback, context, itemStateRetriever);

    profile.onStateUpdateFromHandler(new QuantityType<>(13.0, Units.KILOWATT_HOUR));
    profile.onStateUpdateFromHandler(new QuantityType<>(13.1, Units.KILOWATT_HOUR));
    profile.onStateUpdateFromHandler(new QuantityType<>(10, Units.KILOWATT_HOUR));

    Mockito.verify(callback).sendUpdate(new QuantityType(1013, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType(1026.1, Units.KILOWATT_HOUR));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(1036.1, Units.KILOWATT_HOUR));
  }

}