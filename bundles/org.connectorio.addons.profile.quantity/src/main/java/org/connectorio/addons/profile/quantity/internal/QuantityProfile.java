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

import java.math.BigDecimal;
import java.util.function.Consumer;
import javax.measure.IncommensurableException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Dimensionless;
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

  private void handleReading(Type value, boolean incoming) {
    if (incoming && value instanceof DecimalType) {
      DecimalType decimalReading = (DecimalType) value;

      callback.sendUpdate(new QuantityType<>(decimalReading.toBigDecimal(), unit));
    }
    if (incoming && value instanceof QuantityType) {
      convert((QuantityType<?>) value, (number) -> callback.sendUpdate(new QuantityType<>(number, unit)));
    }
    if (!incoming && value instanceof QuantityType) {
      convert((QuantityType<?>) value, (number) -> callback.handleCommand(new DecimalType(number)));
    }
  }

  private void convert(QuantityType<?> quantifiedReading, Consumer<BigDecimal> callback) {
    logger.debug("Attempting to convert value {} to unit {}", quantifiedReading, unit);
    if (quantifiedReading.getUnit().equals(Units.ONE)) {
      // dimensionless, nothing to do
      callback.accept(quantifiedReading.toBigDecimal());
    }
    try {
      logger.trace("Value require {} conversion to {}, locating unit converter", quantifiedReading, unit);
      UnitConverter converter = quantifiedReading.getUnit().getConverterToAny(unit);
      if (converter != null) {
        if (converter.isIdentity()) {
          callback.accept(quantifiedReading.toBigDecimal());
        } else {
          Number converted = converter.convert(quantifiedReading.toBigDecimal());
          callback.accept(BigDecimal.valueOf(converted.doubleValue()));
        }
        return;
      }
      logger.debug("Unknown conversion from {} to {}", quantifiedReading.getUnit(), unit);
    } catch (IncommensurableException e) {
      logger.warn("Failed to lookup conversion from {} to {}", quantifiedReading.getUnit(), unit, e);
    }
  }


}
