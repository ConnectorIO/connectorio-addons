/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.profile;

import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeUID;

public interface BACnetProfiles {

  ProfileTypeUID RESET_PROFILE_TYPE = new ProfileTypeUID(BACnetBindingConstants.BINDING_ID, "reset");
  ProfileTypeUID PRIORITY_PROFILE_TYPE = new ProfileTypeUID(BACnetBindingConstants.BINDING_ID, "priority");

  ProfileType RESET_PROFILE = ProfileTypeBuilder.newState(RESET_PROFILE_TYPE, "Reset BACnet setpoint")
    .withSupportedChannelTypeUIDs(BACnetBindingConstants.SUPPORTED_CHANNELS)
    .build();

  ProfileType PRIORITY_PROFILE = ProfileTypeBuilder.newState(PRIORITY_PROFILE_TYPE, "BACnet write priority")
    .withSupportedChannelTypeUIDs(BACnetBindingConstants.SUPPORTED_CHANNELS)
    .build();

}
