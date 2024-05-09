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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;
import javax.measure.IncommensurableException;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.Type;

/**
 * A profile which makes sure that item receives only increasing values. It also makes sure that
 * received value is not too great.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
class LimitCounterTopProfile extends BaseCounterProfile {

  public static final BigDecimal _100 = BigDecimal.valueOf(100);
  private final BigDecimal anomaly;

  LimitCounterTopProfile(ProfileCallback callback, ProfileContext context, LinkedItemStateRetriever linkedItemStateRetriever) {
    super(callback, context, linkedItemStateRetriever);

    this.anomaly = BigDecimal.valueOf(Integer.parseInt(context.getConfiguration().get("anomaly").toString()));
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return CounterProfiles.LIMIT_COUNTER_TOP;
  }

  @Override
  protected void handleReading(Type current, Type previous, boolean incoming) {
    if (incoming) { // incoming, handler to item
      if (current instanceof DecimalType && previous instanceof DecimalType) {
        compare((DecimalType) current, (DecimalType) previous, update(callback::sendUpdate));
      }
      if (current instanceof QuantityType && previous instanceof QuantityType<?>) {
        compare((QuantityType<?>) current, (QuantityType<?>) previous, update(callback::sendUpdate));
      }
    } else { // outgoing, item to handler
      if (current instanceof DecimalType && previous instanceof DecimalType) {
        compare((DecimalType) current, (DecimalType) previous, update(callback::handleCommand));
      }
      if (current instanceof QuantityType && previous instanceof QuantityType<?>) {
        compare((QuantityType<?>) current, (QuantityType<?>) previous, update(callback::handleCommand));
      }
    }
  }

  private void compare(DecimalType val, DecimalType last, Consumer<DecimalType> value) {
    BigDecimal currentReading = val.toBigDecimal();
    BigDecimal lastReading = last.toBigDecimal();
    BigDecimal maxIncrease = lastReading.multiply(anomaly).divide(_100, RoundingMode.HALF_UP);
    logger.trace("Verify value {} is smaller than {} + {} ({})%", val, last, maxIncrease, anomaly);
    if (isSmaller(currentReading, lastReading.add(maxIncrease))) {
      value.accept(val);
      return;
    }
    logger.debug("Rejecting value {}, its lower than {}", val, last);
  }

  private <T extends Quantity<T>> void compare(QuantityType<T> val, QuantityType<?> last, Consumer<QuantityType<T>> value) {
    BigDecimal currentReading = convert(val, last.getUnit());
    BigDecimal lastReading = last.toBigDecimal();
    if (currentReading == null) {
      logger.warn("Failed to convert {} to unit compatible with {}.", val, last);
      return;
    }
    BigDecimal maxIncrease = lastReading.multiply(anomaly).divide(_100, RoundingMode.HALF_UP);
    logger.trace("Verify value {} is smaller than {} + {} ({})%", val, last, maxIncrease, anomaly);
    if (isSmaller(currentReading, lastReading.add(maxIncrease))) {
      value.accept(val);
      return;
    }
    logger.debug("Rejecting value {}, its lower than {}", val, last);
  }

  private BigDecimal convert(QuantityType<?> quantifiedReading, Unit<?> unit) {
    logger.debug("Attempting to convert value {} to unit {}", quantifiedReading, unit);
    if (quantifiedReading.getUnit().equals(Units.ONE)) {
      // dimensionless, nothing to do
      return quantifiedReading.toBigDecimal();
    }
    try {
      logger.trace("Value require {} conversion to {}, locating unit converter", quantifiedReading, unit);
      UnitConverter converter = quantifiedReading.getUnit().getConverterToAny(unit);
      if (converter != null) {
        if (converter.isIdentity()) {
          return quantifiedReading.toBigDecimal();
        } else {
          Number converted = converter.convert(quantifiedReading.toBigDecimal());
          return BigDecimal.valueOf(converted.doubleValue());
        }
      }
      logger.debug("Unknown conversion from {} to {}", quantifiedReading.getUnit(), unit);
    } catch (IncommensurableException e) {
      logger.warn("Failed to lookup conversion from {} to {}", quantifiedReading.getUnit(), unit, e);
    }
    return null;
  }

  public String toString() {
    return "LimitCounterTop [" + last() + " " + anomaly + "]";
  }

}

