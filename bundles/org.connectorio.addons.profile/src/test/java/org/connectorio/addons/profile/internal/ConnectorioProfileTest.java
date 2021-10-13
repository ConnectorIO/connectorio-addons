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
package org.connectorio.addons.profile.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.connectorio.addons.profile.ProfileFactoryRegistry;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Test of chained invocations between profiles and framework.
 */
@Disabled
@ExtendWith(MockitoExtension.class)
class ConnectorioProfileTest {

  public static final String CONDITION_PROFILE = "test:condition";
  public static final ProfileTypeUID PROFILE_TYPE_CONDITION = new ProfileTypeUID(CONDITION_PROFILE);
  public static final String SCALE_PROFILE = "test:scale";
  public static final ProfileTypeUID PROFILE_TYPE_SCALE = new ProfileTypeUID(SCALE_PROFILE);

  @Mock
  ProfileFactoryRegistry registry;
  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;
  @Mock
  ProfileFactory factory1;
  @Mock
  ProfileFactory factory2;

  @Test
  void checkChainedValue() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("profiles", Arrays.asList(CONDITION_PROFILE, SCALE_PROFILE));
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    when(registry.getAll()).thenReturn(Arrays.asList(factory1, factory2));

    when(factory1.getSupportedProfileTypeUIDs()).thenReturn(Arrays.asList(PROFILE_TYPE_CONDITION));
    when(factory2.getSupportedProfileTypeUIDs()).thenReturn(Arrays.asList(PROFILE_TYPE_SCALE));
    when(factory1.createProfile(eq(PROFILE_TYPE_CONDITION), any(ProfileCallback.class), any(ProfileContext.class))).thenAnswer((inv) -> new ConditionProfile(
      inv.getArgument(1, ProfileCallback.class),
      inv.getArgument(2, ProfileContext.class)
    ));
    when(factory2.createProfile(eq(PROFILE_TYPE_SCALE), any(ProfileCallback.class), any(ProfileContext.class))).thenAnswer((inv) -> new ScaleProfile(
      inv.getArgument(1, ProfileCallback.class),
      inv.getArgument(2, ProfileContext.class)
    ));

    ConnectorioProfile profile = new ConnectorioProfile(callback, context, registry);
    profile.onCommandFromItem(new DecimalType(22.0));
    Mockito.verify(callback).handleCommand(new DecimalType(11.0));

    profile.onCommandFromHandler(new DecimalType(11.0));
    Mockito.verify(callback).sendUpdate(new DecimalType(22.0));
  }

  class ConditionProfile extends BaseProfile {

    public ConditionProfile(ProfileCallback callback, ProfileContext context) {
      super(context, callback);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
      return PROFILE_TYPE_CONDITION;
    }
    @Override
    public void onStateUpdateFromHandler(State state) {
      if (state instanceof DecimalType) {
        if (((DecimalType) state).doubleValue() > 10) {
          callback.sendUpdate(state);
        }
      }
    }

    @Override
    public void onCommandFromItem(Command command) {
      if (command instanceof DecimalType) {
        if (((DecimalType) command).doubleValue() > 10) {
          callback.handleCommand(command);
        }
      }
    }
  }

  class ScaleProfile extends BaseProfile {

    public ScaleProfile(ProfileCallback callback, ProfileContext context) {
      super(context, callback);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
      return PROFILE_TYPE_SCALE;
    }
    @Override
    public void onStateUpdateFromHandler(State state) {
      if (state instanceof DecimalType) {
        DecimalType dec = (DecimalType) state;
        callback.sendUpdate(new DecimalType(dec.doubleValue() * 2));
      }
    }

    @Override
    public void onCommandFromItem(Command command) {
      if (command instanceof DecimalType) {
        DecimalType dec = (DecimalType) command;
        callback.sendUpdate(new DecimalType(dec.doubleValue() / 2));
      }
    }
  }

  abstract class BaseProfile implements StateProfile {

    final ProfileContext context;
    final ProfileCallback callback;

    public BaseProfile(ProfileContext context, ProfileCallback callback) {
      this.context = context;
      this.callback = callback;
    }

    @Override
    public void onCommandFromItem(Command command) {
    }

    @Override
    public void onCommandFromHandler(Command command) {
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }
  }
}