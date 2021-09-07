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
package org.connectorio.addons.profile.quantity.internal;

import javax.measure.IncommensurableException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QuantityProfile implements StateProfile {

  private final Logger logger = LoggerFactory.getLogger(QuantityProfile.class);
  private final ProfileCallback callback;
  private final ProfileContext context;
  private final Unit<?> unit;

  QuantityProfile(ProfileCallback callback, ProfileContext context) {
    this.callback = callback;
    this.context = context;

    this.unit = UnitUtils.parseUnit(context.getConfiguration().get("unit").toString());
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return QuantityProfiles.QUANTITY;
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

  private void handleReading(Type reading, boolean incoming) {
    if (incoming && reading instanceof DecimalType) {
      DecimalType decimalReading = (DecimalType) reading;

      callback.sendUpdate(new QuantityType<>(decimalReading.toBigDecimal(), unit));
    }
    if (!incoming && reading instanceof QuantityType) {
      QuantityType<?> quantifiedReading = (QuantityType<?>) reading;
      try {
        UnitConverter converter = quantifiedReading.getUnit().getConverterToAny(unit);
        if (converter != null) {
          if (converter.isIdentity()) {
            callback.handleCommand(new DecimalType(quantifiedReading.toBigDecimal()));
          } else {
            Number converted = converter.convert(quantifiedReading.toBigDecimal());
            callback.handleCommand(new DecimalType(converted.doubleValue()));
          }
          return;
        }
        logger.debug("Unknown conversion from {} to {}", quantifiedReading.getUnit(), unit);
      } catch (IncommensurableException e) {
        logger.warn("Failed to lookup conversion from {} to {}", quantifiedReading.getUnit(), unit, e);
      }
    }
  }


}
