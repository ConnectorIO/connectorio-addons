/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional information.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.connectorio.binding.transformation.inverse.profile.toggle;

import static org.connectorio.binding.transformation.inverse.profile.ConnectorioProfiles.TOGGLE_SWITCH_STATE;

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
