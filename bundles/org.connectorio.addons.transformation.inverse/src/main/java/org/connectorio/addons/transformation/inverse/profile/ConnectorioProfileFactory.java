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
package org.connectorio.addons.transformation.inverse.profile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import org.connectorio.addons.transformation.inverse.profile.toggle.ToggleSwitchStateProfile;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;

@Component(service = {ProfileFactory.class, ProfileTypeProvider.class})
public class ConnectorioProfileFactory implements ProfileFactory, ProfileTypeProvider {

  @Override
  public Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback, ProfileContext profileContext) {
    if (ConnectorioProfiles.TOGGLE_SWITCH_STATE.equals(profileTypeUID)) {
      return new ToggleSwitchStateProfile(callback, profileContext);
    }
    return null;
  }

  @Override
  public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
    return Collections.singleton(ConnectorioProfiles.TOGGLE_SWITCH_STATE);
  }

  @Override
  public Collection<ProfileType> getProfileTypes(Locale locale) {
    return Arrays.asList(ConnectorioProfiles.TOGGLE_SWITCH_STATE_TYPE);
  }
}
