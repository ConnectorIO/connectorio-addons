package org.connectorio.addons.binding.amsads.internal.handler.channel;

import org.connectorio.addons.binding.amsads.AmsAdsBindingConstants;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;

public interface AdsChannelHandler {

  ChannelTypeUID CONTACT_SYMBOL = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "contact-symbol");
  ChannelTypeUID CONTACT_DIRECT_HEX = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "contact-direct-hex");
  ChannelTypeUID CONTACT_DIRECT_DEC = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "contact-direct-dec");

  ChannelTypeUID SWITCH_SYMBOL = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "switch-symbol");
  ChannelTypeUID SWITCH_DIRECT_HEX = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "switch-direct-hex");
  ChannelTypeUID SWITCH_DIRECT_DEC = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "switch-direct-dec");

  ChannelTypeUID NUMBER_SYMBOL = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "number-symbol");
  ChannelTypeUID NUMBER_DIRECT_HEX = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "number-direct-hex");
  ChannelTypeUID NUMBER_DIRECT_DEC = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "number-direct-dec");

  Channel createChannel();

}
