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
import java.util.function.Consumer;
import java.util.function.Function;
import javax.measure.Quantity;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.Type;

class PulseCounterProfile extends BaseCounterProfile {

  private final BigDecimal tick;

  PulseCounterProfile(ProfileCallback callback, ProfileContext context, LinkedItemStateRetriever linkedItemStateRetriever) {
    super(callback, context, linkedItemStateRetriever);

    this.tick = new BigDecimal(context.getConfiguration().get("tick").toString());
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return CounterProfiles.PULSE_COUNTER;
  }

  protected void handleReading(Type current, Type previous, boolean incoming) {
    if (incoming) {
      // signal from handler, we got a tick!
      if (current == OpenClosedType.CLOSED || current == OnOffType.ON) {
        if (previous instanceof DecimalType) {
          update(callback::sendUpdate).accept(
            new DecimalType(((DecimalType) previous).toBigDecimal().add(tick))
          );
        }
      }
    }
  }

}
