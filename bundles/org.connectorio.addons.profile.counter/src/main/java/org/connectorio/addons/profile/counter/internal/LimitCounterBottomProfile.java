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

import java.util.function.Consumer;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.Type;

/**
 * A profile which makes sure that item receives only increasing values. It blocks lowering value.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
class LimitCounterBottomProfile extends BaseCounterProfile {

  LimitCounterBottomProfile(ProfileCallback callback, ProfileContext context, LinkedItemStateRetriever itemStateRetriever) {
    super(callback, context, itemStateRetriever);
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return CounterProfiles.LIMIT_COUNTER_BOTTOM;
  }

  @Override
  protected void handleReading(Type current, Type previous, boolean incoming) {
    if (incoming) { // incoming, handler to item
      if (current instanceof DecimalType && last instanceof DecimalType) {
        compare((DecimalType) current, (DecimalType) last, update(callback::sendUpdate));
      }
      if (current instanceof QuantityType && last instanceof QuantityType<?>) {
        compare((QuantityType<?>) current, (QuantityType<?>) last, update(callback::sendUpdate));
      }
    } else { // outgoing, item to handler
      if (current instanceof DecimalType && last instanceof DecimalType) {
        compare((DecimalType) current, (DecimalType) last, update(callback::handleCommand));
      }
      if (current instanceof QuantityType && last instanceof QuantityType<?>) {
        compare((QuantityType<?>) current, (QuantityType<?>) last, update(callback::handleCommand));
      }
    }
  }

  private void compare(DecimalType current, DecimalType previous, Consumer<DecimalType> consumer) {
    if (current.compareTo(previous) >= 0) {
      consumer.accept(current);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void compare(QuantityType current, QuantityType previous, Consumer<QuantityType> consumer) {
    if (current.compareTo(previous) >= 0) {
      consumer.accept(current);
    }
  }

}

