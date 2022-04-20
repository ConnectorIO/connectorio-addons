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
package org.connectorio.addons.profile.boundary.internal;

import java.math.BigDecimal;
import java.util.function.Consumer;
import javax.measure.Unit;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.util.UnitUtils;

public abstract class QuantityLimitProfile extends DecimalLimitProfile {

  private final Unit<?> unit;

  public QuantityLimitProfile(ProfileCallback callback, ProfileContext profileContext, String prefix) {
    super(callback, profileContext, prefix);
    this.unit = UnitUtils.parseUnit(profileContext.getConfiguration().get("unit").toString());
  }

  protected <T extends Type> void evaluate(T current, Consumer<T> consumer) {
    if (current instanceof QuantityType<?>) {
      QuantityType<?> quantity = ((QuantityType<?>) current).toUnit(unit);
      if (quantity != null) {
        BigDecimal value = quantity.toBigDecimal();
        if (evaluate(value, limit)) {
          consumer.accept(current);
        }
      }
    }
  }

  protected abstract boolean evaluate(BigDecimal value, BigDecimal limit);

}
