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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

/**
 * Little utility profile which tracks internally progression of values reported from thing handler.
 * When value decreases it rests its own state to this value and sends it as delta. If value increases
 * it calculates delta from base value registered earlier.
 */
class SustainedCounterProfile extends BaseCounterProfile {

  private AtomicReference<State> previousValue = new AtomicReference<>();

  SustainedCounterProfile(ProfileCallback callback, ProfileContext context, LinkedItemStateRetriever linkedItemStateRetriever) {
    super(true, callback, context, linkedItemStateRetriever);
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return CounterProfiles.SUSTAINED_COUNTER;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void handleReading(Type current, Type previous, boolean incoming) {
    if (incoming) { // incoming, handler to item
      if (current instanceof DecimalType) {
        this.update((AtomicReference) previousValue, (DecimalType) current, (DecimalType) previous, update(callback::sendUpdate),
          (left, right) -> new DecimalType(left.toBigDecimal().subtract(right.toBigDecimal())),
          (left, right) -> new DecimalType(left.toBigDecimal().add(right.toBigDecimal()))
        );
      } else if (current instanceof QuantityType) {
        this.update((AtomicReference) previousValue, (QuantityType) current, (QuantityType) previous, update(callback::sendUpdate),
          QuantityType::subtract,
          QuantityType::add
        );
      }
    }
    // outgoing communication is not supported
  }


  private <T extends Comparable<T>> void update(AtomicReference<T> reference, T current, T previous, Consumer<T> consumer, BiFunction<T, T, T> diff, BiFunction<T, T, T> sum) {
    if (previous == null) {
      if (reference.compareAndSet(null, current)) {
        consumer.accept(current);
        return;
      }
    }

    if (reference.compareAndSet(null, current)) {
      consumer.accept(sum.apply(previous, current));
    } else {
      reference.accumulateAndGet(current, new BinaryOperator<T>() {
        @Override
        public T apply(T prev, T next) {
          T delta = null;
          if (isLargerOrEqual(next, prev)) {
            delta = diff.apply(next, prev);
          } else if (isSmaller(next, prev)) {
            delta = next;
          }
          if (delta != null) {
            consumer.accept(sum.apply(previous, delta));
          }
          return current;
        }
      });
    }
  }

}
