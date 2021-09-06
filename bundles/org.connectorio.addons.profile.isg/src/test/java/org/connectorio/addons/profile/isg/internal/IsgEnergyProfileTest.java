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
package org.connectorio.addons.profile.isg.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;

/**
 * Basic test which confirms conversion logic for energy readings reported by Stiebel ISG device.
 */
@ExtendWith(MockitoExtension.class)
class IsgEnergyProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;

  @Test
  void checkDecimalValue() {
    int val = 0x03D6_002C;
    //           ^    ^
    //           |    |
    //           |    `-- second register value
    //           `------- first register value
    // Above hex combination is read as int 64356396 which should be 44,982 MWh

    IsgEnergyProfile profile = new IsgEnergyProfile(callback, context);
    profile.onStateUpdateFromHandler(new DecimalType(val));

    Mockito.verify(callback).sendUpdate(new QuantityType<>(44.982, Units.MEGAWATT_HOUR));
  }
}