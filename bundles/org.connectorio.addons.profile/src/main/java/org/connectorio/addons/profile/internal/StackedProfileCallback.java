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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stacked callback is a reference point for created profiles to communicate with framework.
 *
 * Since profiles can call callback at any time, this instance must be present when profile is being created.
 * This leads to situation that we have to bridge it into future.
 * More over, because this callback can be called from handler to item and from item to handler
 * it has to work in both directions, independently of the creation time.
 */
public class StackedProfileCallback {

  private final Logger logger = LoggerFactory.getLogger(StackedProfileCallback.class);

  private final ProfileCallback callback;
  private final LinkedList<StateProfile> chain;

  public StackedProfileCallback(ProfileCallback callback, LinkedList<StateProfile> chain) {
    this.callback = callback;
    this.chain = chain;
  }

  public void handleCommand(int index, Command command) {
    if (index == -1) {
      callback.handleCommand(command);
      return;
    }
    logger.trace("Passing command {} to profile chain", command);
    chain.get(index).onCommandFromItem(command);
  }

  public void sendCommand(int index, Command command) {
    if (index == -1) {
      callback.handleCommand(command);
      return;
    }
    logger.trace("Sending command {} toi profile chain", command);
    chain.get(index).onCommandFromHandler(command);
  }

  public void sendTimeSeries(int index, TimeSeries timeSeries) {
    logger.trace("Sending time series {} to system profile callback", timeSeries);
    callback.sendTimeSeries(timeSeries);
  }

  public void sendUpdate(int index, State state) {
    if (index >= chain.size()) {
      callback.sendUpdate(state);
      return;
    }
    logger.trace("Sending state {} to profile chain", state);
    chain.get(index).onStateUpdateFromHandler(state);
  }

}
