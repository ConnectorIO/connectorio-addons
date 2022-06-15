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
package org.connectorio.addons.profile.cast.internal;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.measure.Quantity;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CastBinaryProfile implements StateProfile {

  private final Logger logger = LoggerFactory.getLogger(CastBinaryProfile.class);
  private final ProfileCallback callback;
  private final ProfileContext context;
  private Class<? extends State> type;

  CastBinaryProfile(ProfileCallback callback, ProfileContext context) {
    this.callback = callback;
    this.context = context;

  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return CastProfiles.CAST_BINARY;
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

  private void handleReading(Type val, boolean incoming) {
    if (incoming) { // incoming, handler to item
      if ((val instanceof OnOffType || val instanceof OpenClosedType)) {
        DecimalType decimalVal = ((State) val).as(DecimalType.class);
        if (decimalVal != null) {
          callback.sendUpdate(decimalVal);
        }
      }
    } else { // outgoing, item to handler
      if (val instanceof DecimalType) {
        OnOffType switchVal = ((DecimalType) val).as(OnOffType.class);
        if (switchVal != null) {
          callback.handleCommand(switchVal);
        }
      }
    }
  }

}
