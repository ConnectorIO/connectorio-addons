/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.transformation.inverse.profile.toggle;

import static org.connectorio.addons.transformation.inverse.profile.ConnectorioProfiles.TOGGLE_SWITCH_STATE;

import java.util.Optional;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class ToggleSwitchStateProfile implements StateProfile {

  private final ProfileCallback callback;
  private final ProfileContext context;

  public ToggleSwitchStateProfile(ProfileCallback callback, ProfileContext context) {
    this.callback = callback;
    this.context = context;
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return TOGGLE_SWITCH_STATE;
  }

  @Override
  public void onStateUpdateFromItem(State state) {
    map(state).ifPresent(this.callback::sendUpdate);
  }

  @Override
  public void onCommandFromItem(Command command) {

  }

  @Override
  public void onCommandFromHandler(Command command) {

  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    map(state).ifPresent(this.callback::sendUpdate);
  }

  private Optional<State> map(State type) {
    if (type instanceof OnOffType) {
      return Optional.of(type)
        .map(isOn -> isOn == OnOffType.ON ? OnOffType.OFF : OnOffType.ON);
    }
    return Optional.empty();
  }
}
