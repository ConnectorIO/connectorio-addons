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
package org.connectorio.addons.profile.internal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectorioProfile implements StateProfile {

  private final Logger logger = LoggerFactory.getLogger(ConnectorioProfile.class);
  private final ProfileCallback callback;
  private final ProfileContext context;
  private final LinkedList<StateProfile> profileChain = new LinkedList<>();

  private final Set<ProfileFactory> profileFactories;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  ConnectorioProfile(ProfileCallback callback, ProfileContext context, Set<ProfileFactory> profileFactories) {
    this.callback = callback;
    this.context = context;
    this.profileFactories = profileFactories;

    Object profiles = context.getConfiguration().get("profiles");
    if (!(profiles instanceof List)) {
      throw new IllegalArgumentException("Invalid configuration");
    }

    StackedProfileCallback chainedCallback = new StackedProfileCallback();
    if (profiles instanceof List) {
      List<String> profileList = (List<String>) profiles;
      for (String profile : profileList) {
        Profile createdProfile = getProfileFromFactories(getConfiguredProfileTypeUID(profile), context, chainedCallback);
        if (!(createdProfile instanceof StateProfile)) {
          throw new IllegalArgumentException("Could not create profile " + profile + " or it is not state profile");
        }
        profileChain.add((StateProfile) createdProfile);
      }
    }
  }

  @Override
  public ProfileTypeUID getProfileTypeUID() {
    return ConnectorioProfiles.PROFILE;
  }

  @Override
  public void onStateUpdateFromItem(State state) {
    handleReading(false, (profile) -> profile.onStateUpdateFromItem(state));
  }

  @Override
  public void onCommandFromItem(Command command) {
    handleReading(false, (profile) -> profile.onCommandFromItem(command));
  }

  @Override
  public void onCommandFromHandler(Command command) {
    handleReading(true, (profile) -> profile.onCommandFromHandler(command));
  }

  @Override
  public void onStateUpdateFromHandler(State state) {
    handleReading(true, (profile) -> profile.onStateUpdateFromHandler(state));
  }

  private void handleReading(boolean incoming, Consumer<StateProfile> head) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        Iterator<StateProfile> iterator = incoming ? profileChain.iterator() : profileChain.descendingIterator();
        StackedProfileCallback.set(new ChainedProfileCallback(iterator, callback));
        head.accept(iterator.next());
      }
    });
  }

  private ProfileTypeUID getConfiguredProfileTypeUID(String profileName) {
    if (profileName != null && !profileName.trim().isEmpty()) {
      profileName = normalizeProfileName(profileName);
      return new ProfileTypeUID(profileName);
    }
    return null;
  }

  private String normalizeProfileName(String profileName) {
    if (!profileName.contains(AbstractUID.SEPARATOR)) {
      return ProfileTypeUID.SYSTEM_SCOPE + AbstractUID.SEPARATOR + profileName;
    }
    return profileName;
  }

  private Profile getProfileFromFactories(ProfileTypeUID profileTypeUID, ProfileContext context, ProfileCallback callback) {
    for (ProfileFactory factory : profileFactories) {
      if (supportsProfileTypeUID(factory, profileTypeUID)) {
        logger.trace("using ProfileFactory '{}' to create profile '{}'", factory, profileTypeUID);
        Profile profile = factory.createProfile(profileTypeUID, callback, context);
        if (profile == null) {
          logger.error("ProfileFactory '{}' returned 'null' although it claimed it supports item type '{}'", factory, profileTypeUID);
          return null;
        }
        return profile;
      }
    }

    logger.debug("no ProfileFactory found which supports '{}'", profileTypeUID);
    return null;
  }

  private boolean supportsProfileTypeUID(ProfileFactory profileFactory, ProfileTypeUID profileTypeUID) {
    return profileFactory.getSupportedProfileTypeUIDs().contains(profileTypeUID);
  }

}
