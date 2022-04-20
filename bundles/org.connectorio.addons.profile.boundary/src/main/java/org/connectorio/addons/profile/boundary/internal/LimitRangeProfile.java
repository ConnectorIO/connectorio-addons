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

public class LimitRangeProfile implements StateProfile {

  protected final ProfileCallback callback;
  protected final ProfileContext profileContext;
  protected final BigDecimal lowest;
  protected final BigDecimal highest;

  public LimitRangeProfile(ProfileCallback callback, ProfileContext profileContext) {
    this.callback = callback;
    this.profileContext = profileContext;
    this.lowest = get(profileContext, "lowest");
    this.highest = get(profileContext, "highest");
    if (lowest == null && highest == null) {
      throw new IllegalArgumentException("Required configuration options are missing.");
    }
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return BoundaryProfiles.LIMIT_RANGE;
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
      if ((lowest != null && value.compareTo(lowest) >= 0) && (highest != null && value.compareTo(highest) <= 0)) {
        consumer.accept(current);
      }
    }
  }

  private BigDecimal get(ProfileContext profileContext, String parameter) {
    Object val = profileContext.getConfiguration().get(parameter);
    if (val != null) {
      if (val instanceof BigDecimal) {
        return (BigDecimal) val;
      }
      return new BigDecimal(val.toString());
    }
    return null;
  }

}
