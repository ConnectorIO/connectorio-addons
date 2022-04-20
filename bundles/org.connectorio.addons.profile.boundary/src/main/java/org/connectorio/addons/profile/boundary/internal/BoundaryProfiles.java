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

import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfileType;

public interface BoundaryProfiles {

  ProfileTypeUID LIMIT_RANGE = new ProfileTypeUID("connectorio", "limit-range");
  StateProfileType LIMIT_RANGE_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_RANGE, "Filter values outside range")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID LIMIT_TOP = new ProfileTypeUID("connectorio", "limit-top");
  StateProfileType LIMIT_TOP_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_TOP, "Filter upper numeric values")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID LIMIT_BOTTOM = new ProfileTypeUID("connectorio", "limit-bottom");
  StateProfileType LIMIT_BOTTOM_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_BOTTOM, "Filter lower numeric values")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID LIMIT_RANGE_QUANTITY = new ProfileTypeUID("connectorio", "limit-range-quantity");
  StateProfileType LIMIT_RANGE_QUANTITY_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_RANGE, "Filter quantities outside range")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID LIMIT_TOP_QUANTITY = new ProfileTypeUID("connectorio", "limit-top-quantity");
  StateProfileType LIMIT_TOP_QUANTITY_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_TOP_QUANTITY, "Filter upper quantity values")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();

  ProfileTypeUID LIMIT_BOTTOM_QUANTITY = new ProfileTypeUID("connectorio", "limit-bottom-quantity");
  StateProfileType LIMIT_BOTTOM_QUANTITY_PROFILE_TYPE = ProfileTypeBuilder.newState(LIMIT_BOTTOM_QUANTITY, "Filter lower quantity values")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Number")
    .build();


}