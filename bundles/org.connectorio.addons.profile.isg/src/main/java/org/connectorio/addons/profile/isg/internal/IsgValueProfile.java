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
package org.connectorio.addons.profile.isg.internal;

import javax.measure.quantity.Energy;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

class IsgValueProfile implements StateProfile {

  private final ProfileCallback callback;
  private final ProfileContext context;

  IsgValueProfile(ProfileCallback callback, ProfileContext context) {
    this.callback = callback;
    this.context = context;
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return IsgProfiles.ISG_ENERGY;
  }

  @Override
  public void onStateUpdateFromItem(State state) {
  }

  @Override
  public void onCommandFromItem(Command command) {
  }

  @Override
  public void onCommandFromHandler(Command command) {
    handleDecimalReading(command);
  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    handleDecimalReading(state);
  }

  private void handleDecimalReading(Type reading) {
    if (reading instanceof DecimalType) {
      DecimalType decimalReading = (DecimalType) reading;

      long value = decimalReading.toBigDecimal().longValue();
      if (value <= 0x8000L) {
        if (value != 0x8000L) {
          callback.sendUpdate(decimalReading);
        }
      } else {
        if (value != 0x8000_8000L) {
          callback.sendUpdate(decimalReading);
        }
      }
    }
  }


}
