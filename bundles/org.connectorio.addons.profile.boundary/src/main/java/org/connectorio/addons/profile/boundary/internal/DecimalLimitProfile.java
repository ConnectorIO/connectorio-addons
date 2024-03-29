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
package org.connectorio.addons.profile.boundary.internal;

import java.math.BigDecimal;
import java.util.function.Consumer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecimalLimitProfile implements StateProfile {

  public final static String TOP = "top";
  public final static String BOTTOM = "bottom";

  protected final Logger logger = LoggerFactory.getLogger(DecimalLimitProfile.class);
  protected final ProfileCallback callback;
  protected final ProfileContext profileContext;
  protected final BigDecimal limit;

  public DecimalLimitProfile(ProfileCallback callback, ProfileContext profileContext, String prefix) {
    this.callback = callback;
    this.profileContext = profileContext;
    this.limit = new BigDecimal(profileContext.getConfiguration().get(prefix + "Limit").toString());
  }

  @Override
  public void onCommandFromItem(Command command) {
    evaluate(command, callback::handleCommand);
  }

  @Override
  public void onCommandFromHandler(Command command) {
    evaluate(command, callback::sendCommand);
  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    evaluate(state, callback::sendUpdate);
  }

  @Override
  public void onStateUpdateFromItem(State state) {
    if (state instanceof Command) {
      evaluate((Command) state, callback::handleCommand);
    }
  }

  protected <T extends Type> void evaluate(T current, Consumer<T> consumer) {
    if (current instanceof DecimalType) {
      BigDecimal value = ((DecimalType) current).toBigDecimal();
      if (evaluate(value, limit)) {
        consumer.accept(current);
        return;
      }
      logger.debug("Refusing value {}, it does not pass limit condition {}", current, limit);
    }
  }

  protected abstract boolean evaluate(BigDecimal value, BigDecimal limit);

}
