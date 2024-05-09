/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
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

import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Callback implementation which is aware of its position in chain.
 *
 * When this callback is asked to dispatch command or sate it passes it to chain with upper (state)
 * or lower (command) element index. This construction allows to move state/command information
 * across entire chain without too complex logic. Finalization of the call happens n stacked profile
 * callback which know chain boundaries.
 */
public class NavigableCallback implements ProfileCallback {

  private final ItemChannelLink link;
  private final int index;
  private final StackedProfileCallback stack;

  public NavigableCallback(ItemChannelLink link, int index, StackedProfileCallback stack) {
    this.link = link;
    this.index = index;
    this.stack = stack;
  }

  @Override
  public void handleCommand(Command command) {
    stack.handleCommand(index - 1, command);
  }

  @Override
  public void sendCommand(Command command) {
    stack.sendCommand(index - 1, command);
  }

  @Override
  public void sendUpdate(State state) {
    stack.sendUpdate(index + 1, state);
  }

  @Override
  public String toString() {
    return "Chained Callback [" + link + " at index " + index + "]";
  }

}
