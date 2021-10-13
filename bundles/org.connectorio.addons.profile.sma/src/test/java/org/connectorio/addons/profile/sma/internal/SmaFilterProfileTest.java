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
package org.connectorio.addons.profile.sma.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.UnDefType;

@ExtendWith(MockitoExtension.class)
class SmaFilterProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;

  @Test
  void checkDecimalValue() {
    SmaFilterProfile profile = new SmaFilterProfile(callback, context);

    // filter out value
    profile.onStateUpdateFromHandler(new DecimalType(0x8000));
    verify(callback).sendUpdate(UnDefType.UNDEF);

    profile.onStateUpdateFromHandler(new DecimalType(10.0));
    verify(callback).sendUpdate(new DecimalType(10.0));

  }
}