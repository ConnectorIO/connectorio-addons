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

import java.time.Clock;
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
 * Basic test which confirms conversion logic for energy counter.
 */
@ExtendWith(MockitoExtension.class)
class EnergyCounterProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;
  @Mock
  Clock clock;

  @Test
  void checkDecimal() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    Configuration config = new Configuration(cfgMap);
    when(context.getConfiguration()).thenReturn(config);

    EnergyCounterProfile profile = new EnergyCounterProfile(callback, context, clock);

    // update from item above accepted level
    when(clock.millis()).thenReturn(0L);
    profile.onStateUpdateFromHandler(new DecimalType(1));

    when(clock.millis()).thenReturn(60 * 60 * 1000L);
    profile.onStateUpdateFromHandler(new DecimalType(1));

    Mockito.verify(callback).sendUpdate(new QuantityType<>(1, Units.WATT_HOUR));
  }

  @Test
  void checkQuantity() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    Configuration config = new Configuration(cfgMap);
    when(context.getConfiguration()).thenReturn(config);

    // we shall start with 10.0 retrieved from persistence
    EnergyCounterProfile profile = new EnergyCounterProfile(callback, context, clock);

    // update from item above accepted level
    when(clock.millis()).thenReturn(0L);
    profile.onStateUpdateFromHandler(new QuantityType<>(1, Units.WATT));

    when(clock.millis()).thenReturn(60 * 60 * 1000L);
    profile.onStateUpdateFromHandler(new QuantityType<>(1, Units.WATT));

    Mockito.verify(callback).sendUpdate(new QuantityType<>(1, Units.WATT_HOUR));
  }

}