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

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;

@Component(service = {ProfileFactory.class, ProfileTypeProvider.class})
public class BoundaryProfileFactory implements ProfileFactory, ProfileTypeProvider {

  @Override
  public Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback, ProfileContext profileContext) {
    if (BoundaryProfiles.LIMIT_BOTTOM.equals(profileTypeUID)) {
      return new LimitBottomProfile(callback, profileContext);
    }
    if (BoundaryProfiles.LIMIT_BOTTOM_QUANTITY.equals(profileTypeUID)) {
      return new LimitBottomQuantityProfile(callback, profileContext);
    }
    if (BoundaryProfiles.LIMIT_TOP.equals(profileTypeUID)) {
      return new LimitTopProfile(callback, profileContext);
    }
    if (BoundaryProfiles.LIMIT_TOP_QUANTITY.equals(profileTypeUID)) {
      return new LimitTopQuantityProfile(callback, profileContext);
    }
    return null;
  }

  @Override
  public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
    return Arrays.asList(BoundaryProfiles.LIMIT_RANGE, BoundaryProfiles.LIMIT_BOTTOM, BoundaryProfiles.LIMIT_BOTTOM_QUANTITY,
      BoundaryProfiles.LIMIT_RANGE_QUANTITY, BoundaryProfiles.LIMIT_TOP, BoundaryProfiles.LIMIT_TOP_QUANTITY);
  }

  @Override
  public Collection<ProfileType> getProfileTypes(Locale locale) {
    return Arrays.asList(BoundaryProfiles.LIMIT_RANGE_PROFILE_TYPE, BoundaryProfiles.LIMIT_BOTTOM_PROFILE_TYPE, BoundaryProfiles.LIMIT_BOTTOM_QUANTITY_PROFILE_TYPE,
      BoundaryProfiles.LIMIT_RANGE_QUANTITY_PROFILE_TYPE, BoundaryProfiles.LIMIT_TOP_PROFILE_TYPE, BoundaryProfiles.LIMIT_TOP_QUANTITY_PROFILE_TYPE);
  }

}
