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
package org.connectorio.addons.profile.boundary.internal;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import javax.measure.MetricPrefix;
import javax.measure.Unit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import tech.units.indriya.unit.Units;
import javax.measure.MetricPrefix;
import javax.measure.Unit;

@ExtendWith(MockitoExtension.class)
public class LimitBottomQuantityProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;

  @Test
  void checkQuantityValues() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("bottomLimit", "10.0");
    cfgMap.put("unit", "W");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    LimitBottomQuantityProfile profile = new LimitBottomQuantityProfile(callback, context);
    profile.onStateUpdateFromHandler(new QuantityType<>(9.0, Units.WATT));
    Mockito.verifyNoInteractions(callback);

    profile.onCommandFromItem(new QuantityType<>(9.0, Units.WATT));
    Mockito.verifyNoInteractions(callback);

    profile.onCommandFromItem(new QuantityType<>(10.001, Units.WATT));
    Mockito.verify(callback).handleCommand(new QuantityType<>(10.001, Units.WATT));

    Unit<?> milliWatt = MetricPrefix.MILLI(Units.WATT);
    profile.onCommandFromHandler(new QuantityType<>(10002, milliWatt));
    Mockito.verify(callback).sendCommand(new QuantityType<>(10002, milliWatt));
  }

}
