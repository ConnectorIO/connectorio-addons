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
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;

public class LimitBottomQuantityProfile extends QuantityLimitProfile implements StateProfile {

  public LimitBottomQuantityProfile(ProfileCallback callback, ProfileContext profileContext) {
    super(callback, profileContext, BOTTOM);
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return BoundaryProfiles.LIMIT_BOTTOM_QUANTITY;
  }

  @Override
  protected boolean evaluate(BigDecimal value, BigDecimal limit) {
    return value.compareTo(limit) >= 0;
  }

}
