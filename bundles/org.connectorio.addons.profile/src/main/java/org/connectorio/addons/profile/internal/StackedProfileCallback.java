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

import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class StackedProfileCallback implements ProfileCallback {

  private final static ThreadLocal<ProfileCallback> DELEGATE = new ThreadLocal<>();

  @Override
  public void handleCommand(Command command) {
    getDelegate().handleCommand(command);
  }

  @Override
  public void sendCommand(Command command) {
    getDelegate().sendCommand(command);
  }

  @Override
  public void sendUpdate(State state) {
    getDelegate().sendUpdate(state);
  }

  private ProfileCallback getDelegate() {
    ProfileCallback callback = DELEGATE.get();
    if (callback != null) {
      return callback;
    }

    throw new IllegalStateException("No callback found on thread stack");
  }

  static void set(ProfileCallback callback) {
    DELEGATE.set(callback);
  }

}
