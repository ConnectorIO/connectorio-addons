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
package org.connectorio.addons.profile.timer.internal;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.internal.ConfigMapper;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;

/**
 * Test of debouncing profile.
 */
@ExtendWith(MockitoExtension.class)
class DebounceProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;

  @Test
  void checkFirstValue() throws Exception {
    Map<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("unit", TimeUnit.MILLISECONDS.name());
    cfgMap.put("delay", "500");
    cfgMap.put("first", "true");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    ScheduledDebounceManager manager = new ScheduledDebounceManager();
    DebounceProfile profile = new DebounceProfile(callback, context, manager);
    profile.onStateUpdateFromHandler(OnOffType.OFF);
    profile.onStateUpdateFromHandler(OnOffType.ON);
    Mockito.verifyNoInteractions(callback);
    Thread.sleep(500);
    Mockito.verify(callback).sendUpdate(OnOffType.OFF);
    Mockito.verifyNoMoreInteractions(callback);
  }

  @Test
  void checkLastValue() throws Exception {
    Map<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("unit", TimeUnit.MILLISECONDS.name());
    cfgMap.put("delay", "500");
    cfgMap.put("first", "false");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    ScheduledDebounceManager manager = new ScheduledDebounceManager();
    DebounceProfile profile = new DebounceProfile(callback, context, manager);
    profile.onStateUpdateFromHandler(OnOffType.OFF);
    profile.onStateUpdateFromHandler(OnOffType.ON);
    Mockito.verifyNoInteractions(callback);
    Thread.sleep(500);
    Mockito.verify(callback).sendUpdate(OnOffType.ON);
    Mockito.verifyNoMoreInteractions(callback);
  }

}