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
package org.connectorio.addons.profile.counter.internal;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import org.connectorio.addons.profile.counter.internal.state.DummyStateRetriever;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.Type;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.ProductUnit;

/**
 * Little utility profile which, based on power reading will calculate energy consumption.
 */
class EnergyCounterProfile extends BaseCounterProfile {

  // helper unit used internally by profile
  public static Unit<Energy> MILLISECOND_WATT = new ProductUnit<>(Units.WATT.multiply(MetricPrefix.MILLI(Units.SECOND)));
  private final Clock clock;

  static class Measurement {
    final BigDecimal watts;
    final long timestamp;

    Measurement(BigDecimal watts, long timestamp) {
      this.watts = watts;
      this.timestamp = timestamp;
    }
  }

  private AtomicReference<Measurement> previousValue = new AtomicReference<>();

  EnergyCounterProfile(ProfileCallback callback, ProfileContext context) {
    this(callback, context, Clock.systemUTC());
  }

  EnergyCounterProfile(ProfileCallback callback, ProfileContext context, Clock clock) {
    super(true, callback, context, new DummyStateRetriever());
    this.clock = clock;
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return CounterProfiles.ENERGY_COUNTER;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void handleReading(Type current, Type previous, boolean incoming) {
    long timestamp = clock.millis();
    if (incoming) { // incoming, handler to item
      if (current instanceof DecimalType) {
        // assume that number we received express power in Watts
        Measurement measurement = new Measurement(((DecimalType) current).toBigDecimal(), timestamp);
        this.update(measurement, update(callback::sendUpdate));
      } else if (current instanceof QuantityType) {
        QuantityType value = ((QuantityType) current).toUnit(Units.WATT);
        if (value != null) {;
          this.update(new Measurement(value.toBigDecimal(), timestamp), update(callback::sendUpdate));
        }
      }
    }
    // outgoing communication is not supported
  }

  private void update(Measurement power, Consumer<QuantityType<Energy>> consumer) {
    if (previousValue.compareAndSet(null, power)) {
      // initialization
      return;
    }

    previousValue.getAndAccumulate(power, new BinaryOperator<Measurement>() {
      @Override
      public Measurement apply(Measurement previous, Measurement current) {
        long millis = current.timestamp - previous.timestamp;

        QuantityType<Energy> consumption = new QuantityType<>(previous.watts.multiply(BigDecimal.valueOf(millis)), MILLISECOND_WATT);
        consumer.accept(consumption.toUnit(Units.WATT_HOUR));

        return current;
      }
    });
  }

}
