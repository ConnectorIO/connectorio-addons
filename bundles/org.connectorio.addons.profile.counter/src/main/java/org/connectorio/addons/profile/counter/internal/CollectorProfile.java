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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.Type;

/**
 * Profile which will keep collecting received values and adding them to initial item state.
 */
class CollectorProfile extends BaseCounterProfile {

  CollectorProfile(ProfileCallback callback, ProfileContext context, LinkedItemStateRetriever linkedItemStateRetriever) {
    super(true, callback, context, linkedItemStateRetriever);
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return CounterProfiles.COLLECTOR; // TODO
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void handleReading(Type current, Type previous, boolean incoming) {
    if (incoming) { // incoming, handler to item
      if (current instanceof DecimalType) {
        this.update((DecimalType) current, (DecimalType) previous, update(callback::sendUpdate),
          (left, right) -> new DecimalType(left.toBigDecimal().add(right.toBigDecimal()))
        );
      } else if (current instanceof QuantityType) {
        this.update((QuantityType) current, (QuantityType) previous, update(callback::sendUpdate),
          QuantityType::add
        );
      }
    }
    // outgoing communication is not supported
  }

  private <T extends Comparable<T>> void update(T current, T previous, Consumer<T> consumer, BiFunction<T, T, T> sum) {
    if (previous == null) {
      // initialization
      consumer.accept(current);
      return;
    }
    T collected = sum.apply(previous, current);
    consumer.accept(collected);
  }

}
