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
class IsgValueProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;

  @Test
  void checkDecimalValue() {
    int val = 0x8000; // single empty value
    IsgValueProfile profile = new IsgValueProfile(callback, context);
    DecimalType state = new DecimalType(val);

    profile.onStateUpdateFromHandler(state);
    Mockito.verifyNoMoreInteractions(callback);

    state = new DecimalType(val -1);
    profile.onStateUpdateFromHandler(state);
    Mockito.verify(callback).sendUpdate(state);
  }

  @Test
  void checkBigDecimalValue() {
    // two registers, both are empty!
    long val = 0x8000_8000L;
    IsgValueProfile profile = new IsgValueProfile(callback, context);
    DecimalType state = new DecimalType(val);

    profile.onStateUpdateFromHandler(state);
    Mockito.verifyNoMoreInteractions(callback);

    state = new DecimalType(val -1);
    profile.onStateUpdateFromHandler(state);
    Mockito.verify(callback).sendUpdate(state);
  }
}