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

import java.util.Optional;
import org.code_house.bacnet4j.wrapper.api.Priorities;
import org.code_house.bacnet4j.wrapper.api.Priority;
import org.connectorio.addons.binding.bacnet.internal.config.PriorityProfileConfig;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public abstract class BasePriorityProfile implements StateProfile {

  private final ProfileTypeUID channelType;
  protected final ProfileCallback callback;
  protected final ProfileContext context;
  protected final Priority priority;

  public BasePriorityProfile(ProfileTypeUID channelType, ProfileCallback callback, ProfileContext context) {
    this.channelType = channelType;
    this.callback = callback;
    this.context = context;

    priority = Optional.ofNullable(context.getConfiguration().as(PriorityProfileConfig.class))
      .map(cfg -> cfg.priority)
      .flatMap(Priorities::get)
      .orElse(null);
  }

  @Override
  public void onStateUpdateFromItem(State state) {
    if (state instanceof Command) {
      onCommandFromItem((Command) state);
    }
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return channelType;
  }

}
