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
package org.connectorio.addons.profile.cast.internal;

import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfileType;

public interface CastProfiles {

  ProfileTypeUID CAST_BINARY = new ProfileTypeUID("connectorio", "cast-binary");

  StateProfileType CAST_BINARY_PROFILE_TYPE = ProfileTypeBuilder.newState(CAST_BINARY, "Cast binary commands/states")
    .withSupportedItemTypes("Number")
    .withSupportedItemTypesOfChannel("Contact", "Switch")
    .build();

}