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

import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfileType;

public interface CounterProfiles {

  ProfileTypeUID COLLECTOR = new ProfileTypeUID("connectorio", "collector");
  StateProfileType COLLECTOR_PROFILE_TYPE = ProfileTypeBuilder.newState(COLLECTOR, "Collector")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID ENERGY_COUNTER = new ProfileTypeUID("connectorio", "energy-counter");
  StateProfileType ENERGY_COUNTER_PROFILE_TYPE = ProfileTypeBuilder.newState(COLLECTOR, "Energy Counter (transform W to Wh)")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID LIMIT_COUNTER_TOP = new ProfileTypeUID("connectorio", "limit-counter-top");
  StateProfileType LIMIT_COUNTER_TOP_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_COUNTER_TOP, "Filter counter upper values")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID LIMIT_COUNTER_BOTTOM = new ProfileTypeUID("connectorio", "limit-counter-bottom");
  StateProfileType LIMIT_COUNTER_BOTTOM_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_COUNTER_TOP, "Filter counter lower values")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID PULSE_COUNTER = new ProfileTypeUID("connectorio", "pulse-counter");
  StateProfileType PULSE_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_COUNTER_TOP, "Pulse (tick) counter")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Switch", "Contact")
    .build();

  ProfileTypeUID SUSTAINED_COUNTER = new ProfileTypeUID("connectorio", "sustained-counter");
  StateProfileType SUSTAINED_COUNTER_PROFILE_TYPE = ProfileTypeBuilder.newState(SUSTAINED_COUNTER, "Sustained counter")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();
}