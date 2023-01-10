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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainedProfileCallback implements ProfileCallback {

  private final Logger logger = LoggerFactory.getLogger(ChainedProfileCallback.class);
  private final Iterator<StateProfile> profiles;
  private final ProfileCallback delegate;

  public ChainedProfileCallback(Iterator<StateProfile> profiles, ProfileCallback delegate) {
    this.profiles = profiles;
    this.delegate = delegate;
  }

  @Override
  public void handleCommand(Command command) {
    if (profiles.hasNext()) {
      StateProfile next = profiles.next();
      logger.trace("Passing command {} to next profile {}", command, next);
      next.onCommandFromItem(command);
    } else {
      logger.trace("Passing command {} final callback", command);
      delegate.handleCommand(command);
    }
  }

  @Override
  public void sendCommand(Command command) {
    if (profiles.hasNext()) {
      StateProfile next = profiles.next();
      logger.trace("Sending command {} to next profile {}", command, next);
      next.onCommandFromHandler(command);
    } else {
      logger.trace("Sending command {} to final callback", command);
      delegate.sendCommand(command);
    }
  }

  @Override
  public void sendUpdate(State state) {
    if (profiles.hasNext()) {
      StateProfile next = profiles.next();
      logger.trace("Sending state {} to next profile {}", state, next);
      next.onStateUpdateFromHandler(state);
    } else {
      logger.trace("Sending state {} to final callback", state);
      delegate.sendUpdate(state);
    }
  }

  public String toString() {
    return "ChainedProfileCallback [" + profiles + ", " + delegate + "]";
  }
}
