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
package org.connectorio.addons.profile.cast.internal;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;

/**
 * Basic test which confirms conversion logic for number/binary values.
 */
@ExtendWith(MockitoExtension.class)
class CastBinaryProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;

  @Test
  void checkDecimalValue() {
    CastBinaryProfile profile = new CastBinaryProfile(callback, context);
    profile.onStateUpdateFromHandler(OnOffType.ON);
    Mockito.verify(callback).sendUpdate(new DecimalType(1));
    profile.onStateUpdateFromHandler(OnOffType.OFF);
    Mockito.verify(callback).sendUpdate(new DecimalType(0));

    Mockito.reset(callback);

    // outgoing -> multiply
    profile.onStateUpdateFromHandler(OpenClosedType.CLOSED);
    Mockito.verify(callback).sendUpdate(new DecimalType(0));
    profile.onStateUpdateFromHandler(OpenClosedType.OPEN);
    Mockito.verify(callback).sendUpdate(new DecimalType(1));
  }
}