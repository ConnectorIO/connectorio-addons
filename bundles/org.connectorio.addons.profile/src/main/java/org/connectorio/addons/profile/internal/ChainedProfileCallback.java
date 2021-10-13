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

import java.util.Iterator;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class ChainedProfileCallback implements ProfileCallback {

  private final Iterator<StateProfile> profiles;
  private final ProfileCallback delegate;

  public ChainedProfileCallback(Iterator<StateProfile> profiles, ProfileCallback delegate) {
    this.profiles = profiles;
    this.delegate = delegate;
  }

  @Override
  public void handleCommand(Command command) {
    if (profiles.hasNext()) {
      profiles.next().onCommandFromItem(command);
    } else {
      delegate.handleCommand(command);
    }
  }

  @Override
  public void sendCommand(Command command) {
    if (profiles.hasNext()) {
      profiles.next().onCommandFromHandler(command);
    } else {
      delegate.sendCommand(command);
    }
  }

  @Override
  public void sendUpdate(State state) {
    if (profiles.hasNext()) {
      profiles.next().onStateUpdateFromHandler(state);
    } else {
      delegate.sendUpdate(state);
    }
  }
}
