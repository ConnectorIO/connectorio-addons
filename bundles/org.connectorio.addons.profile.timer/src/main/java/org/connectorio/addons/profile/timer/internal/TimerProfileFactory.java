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
package org.connectorio.addons.profile.timer.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import org.connectorio.addons.profile.timer.DebounceManager;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = {ProfileFactory.class, ProfileTypeProvider.class})
public class TimerProfileFactory implements ProfileFactory, ProfileTypeProvider {

  private final DebounceManager debounceManager;

  @Activate
  public TimerProfileFactory(@Reference DebounceManager debounceManager) {
    this.debounceManager = debounceManager;
  }

  @Override
  public Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback, ProfileContext profileContext) {
    if (TimerProfiles.DEBOUNCE.equals(profileTypeUID)) {
      return new DebounceProfile(callback, profileContext, debounceManager);
    }
    return null;
  }

  @Override
  public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
    return Arrays.asList(TimerProfiles.DEBOUNCE);
  }

  @Override
  public Collection<ProfileType> getProfileTypes(Locale locale) {
    return Arrays.asList(TimerProfiles.DEBOUNCE_PROFILE_TYPE);
  }

}
