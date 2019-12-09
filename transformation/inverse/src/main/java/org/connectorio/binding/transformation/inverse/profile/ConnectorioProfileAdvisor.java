package org.connectorio.binding.transformation.inverse.profile;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class ConnectorioProfileAdvisor implements ProfileAdvisor {

  @Override
  public ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, String itemType) {
    if (channel.getKind() == ChannelKind.STATE && "switch".equalsIgnoreCase(itemType)) {
      return ConnectorioProfiles.TOGGLE_SWITCH_STATE;
    }
    return null;
  }

  @Override
  public ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, String itemType) {
    if (channelType.getKind() == ChannelKind.STATE && "switch".equalsIgnoreCase(itemType)) {
      return ConnectorioProfiles.TOGGLE_SWITCH_STATE;
    }
    return null;
  }
}
