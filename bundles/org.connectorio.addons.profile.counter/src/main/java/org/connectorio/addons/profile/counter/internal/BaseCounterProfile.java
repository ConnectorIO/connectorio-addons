/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.profile.counter.internal;

import java.util.function.Consumer;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCounterProfile implements StateProfile {

  protected final Logger logger = LoggerFactory.getLogger(BaseCounterProfile.class);
  protected final ProfileCallback callback;
  protected final UninitializedBehavior uninitializedBehavior;
  protected final ProfileContext context;
  protected Type last;

  protected BaseCounterProfile(ProfileCallback callback, ProfileContext context, LinkedItemStateRetriever linkedItemStateRetriever) {
    this.callback = callback;
    this.uninitializedBehavior = UninitializedBehavior.parse(context.getConfiguration().get("uninitializedBehavior"));
    this.context = context;
    if (UninitializedBehavior.RESTORE_FROM_PERSISTENCE.equals(uninitializedBehavior)) {
      logger.info("Initialized last state of counter");
      String itemName = linkedItemStateRetriever.getItemName(callback);
      if (itemName == null) {
        logger.error("Could not determine item name for callback {}", callback);
      } else {
        last = linkedItemStateRetriever.retrieveState(itemName);
        logger.info("Initialized link with initial state {}", last);
      }
    }
  }

  @Override
  public void onStateUpdateFromItem(State state) {
    handleReading(state, false);
  }

  @Override
  public void onCommandFromItem(Command command) {
    handleReading(command, false);
  }

  @Override
  public void onCommandFromHandler(Command command) {
    handleReading(command, true);
  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    handleReading(state, true);
  }

  protected <T extends Type> Consumer<T> update(Consumer<T> target) {
    return new Consumer<T>() {
      @Override
      public void accept(T t) {
        target.accept(t);
        last = t;
      }
    };
  }

  private void handleReading(Type val, boolean incoming) {
    logger.trace("Verify reading {} vs {}. Value received from handler: {}", val, last, incoming);
    if (last == null) {
      if (uninitializedBehavior == UninitializedBehavior.RESTORE_FROM_ITEM) {
        if (!incoming && val instanceof Command) {
          last = val;
          callback.handleCommand((Command) val);
          return;
        }
      }
      if (uninitializedBehavior == UninitializedBehavior.RESTORE_FROM_HANDLER) {
        if (incoming && val instanceof State) {
          last = val;
          callback.sendUpdate((State) val);
          return;
        }
      }
      return;
    }

    handleReading(val, last, incoming);
  }

  protected abstract void handleReading(Type current, Type previous, boolean incoming);

  enum UninitializedBehavior {
    RESTORE_FROM_ITEM, RESTORE_FROM_PERSISTENCE, RESTORE_FROM_HANDLER;

    public static UninitializedBehavior parse(Object behavior) {
      if (!(behavior instanceof String)) {
        return RESTORE_FROM_ITEM;
      }
      if (RESTORE_FROM_ITEM.name().equals(behavior)) {
        return RESTORE_FROM_ITEM;
      }
      if (RESTORE_FROM_PERSISTENCE.name().equals(behavior)) {
        return RESTORE_FROM_PERSISTENCE;
      }
      if (RESTORE_FROM_HANDLER.name().equals(behavior)) {
        return RESTORE_FROM_HANDLER;
      }
      return RESTORE_FROM_ITEM;
    }
  }
}
