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
package org.connectorio.addons.profile.quantity.internal;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
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
 * Basic test which confirms conversion logic for quantity/no quantity reading.
 */
@ExtendWith(MockitoExtension.class)
class QuantityProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;

  @Test
  void checkDecimalValue() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("unit", "B");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);
    QuantityProfile profile = new QuantityProfile(callback, context);

    profile.onStateUpdateFromHandler(new DecimalType(10.0));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(10.0, Units.BYTE));

    profile.onStateUpdateFromHandler(new QuantityType<>(8, Units.BIT));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(1.0, Units.BYTE));

    profile.onCommandFromItem(new QuantityType<>(8, Units.BIT));
    Mockito.verify(callback).handleCommand(new DecimalType(1.0));
  }

  @Test
  void checkDimensionless() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("unit", "B");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);
    QuantityProfile profile = new QuantityProfile(callback, context);

    profile.onStateUpdateFromHandler(new QuantityType<>(10, Units.ONE));
    Mockito.verify(callback).sendUpdate(new QuantityType<>(10, Units.BYTE));

    profile.onCommandFromItem(new QuantityType<>(10, Units.ONE));
    Mockito.verify(callback).handleCommand(new DecimalType(10));
  }
}