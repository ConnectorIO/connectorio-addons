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

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
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
public class CounterProfileFactory implements ProfileFactory, ProfileTypeProvider {

  private final LinkedItemStateRetriever linkedItemStateRetriever;

  @Activate
  public CounterProfileFactory(@Reference LinkedItemStateRetriever linkedItemStateRetriever) {
    this.linkedItemStateRetriever = linkedItemStateRetriever;
  }

  @Override
  public Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback, ProfileContext profileContext) {
    if (CounterProfiles.COLLECTOR.equals(profileTypeUID)) {
      return new CollectorProfile(callback, profileContext, linkedItemStateRetriever);
    }
    if (CounterProfiles.ENERGY_COUNTER.equals(profileTypeUID)) {
      return new EnergyCounterProfile(callback, profileContext);
    }
    if (CounterProfiles.LIMIT_COUNTER_TOP.equals(profileTypeUID)) {
      return new LimitCounterTopProfile(callback, profileContext, linkedItemStateRetriever);
    }
    if (CounterProfiles.LIMIT_COUNTER_BOTTOM.equals(profileTypeUID)) {
      return new LimitCounterBottomProfile(callback, profileContext, linkedItemStateRetriever);
    }
    if (CounterProfiles.PULSE_COUNTER.equals(profileTypeUID)) {
      return new PulseCounterProfile(callback, profileContext, linkedItemStateRetriever);
    }
    if (CounterProfiles.SUSTAINED_COUNTER.equals(profileTypeUID)) {
      return new SustainedCounterProfile(callback, profileContext, linkedItemStateRetriever);
    }
    return null;
  }

  @Override
  public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
    return Arrays.asList(CounterProfiles.COLLECTOR, CounterProfiles.ENERGY_COUNTER,
      CounterProfiles.LIMIT_COUNTER_TOP, CounterProfiles.LIMIT_COUNTER_BOTTOM,
      CounterProfiles.PULSE_COUNTER, CounterProfiles.SUSTAINED_COUNTER
    );
  }

  @Override
  public Collection<ProfileType> getProfileTypes(Locale locale) {
    return Arrays.asList(CounterProfiles.COLLECTOR_PROFILE_TYPE, CounterProfiles.ENERGY_COUNTER_PROFILE_TYPE,
      CounterProfiles.LIMIT_COUNTER_TOP_PROFILE_TYPE, CounterProfiles.LIMIT_COUNTER_BOTTOM_PROFILE_TYPE,
      CounterProfiles.PULSE_PROFILE_TYPE, CounterProfiles.SUSTAINED_COUNTER_PROFILE_TYPE
    );
  }

}
