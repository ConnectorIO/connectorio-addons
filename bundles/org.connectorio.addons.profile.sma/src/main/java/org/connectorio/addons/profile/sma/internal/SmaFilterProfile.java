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

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SmaFilterProfile implements StateProfile {

  private final ProfileCallback callback;
  private Logger logger = LoggerFactory.getLogger(SmaFilterProfile.class);

  SmaFilterProfile(ProfileCallback callback, ProfileContext context) {
    this.callback = callback;
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return SmaProfiles.SMA_FILTER;
  }

  @Override
  public void onStateUpdateFromItem(State state) {
  }

  @Override
  public void onCommandFromItem(Command command) {
  }

  @Override
  public void onCommandFromHandler(Command command) {
    if (command instanceof State) {
      onStateUpdateFromHandler((State) command);
    }
  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    if (state instanceof DecimalType) {
      DecimalType value = (DecimalType) state;
      int intVal = value.toBigDecimal().intValue();
      if (intVal == 0x80000000 || intVal == 0x8000) {
        logger.debug("SMA filter detected undef value {} / {}", state, Integer.toHexString(value.toBigDecimal().intValue()));
        callback.sendUpdate(UnDefType.UNDEF);
        return;
      }
      callback.sendUpdate(state);
    }
  }

}
