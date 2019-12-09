package org.connectorio.binding.transformation.inverse.profile;

import org.eclipse.smarthome.core.thing.profiles.ProfileTypeBuilder;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfileType;

public interface ConnectorioProfiles {

  ProfileTypeUID TOGGLE_SWITCH_STATE = new ProfileTypeUID("connectorio", "rawbutton-toggle-switch");

  StateProfileType TOGGLE_SWITCH_STATE_TYPE = ProfileTypeBuilder.newState(TOGGLE_SWITCH_STATE, "Toggle")
    .withSupportedItemTypes("Switch")
    .withSupportedItemTypesOfChannel("Switch")
    .build();

}