/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.amsads.internal.handler.channel;

import org.apache.plc4x.java.ads.tag.AdsTag;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteRequest.Builder;
import org.apache.plc4x.java.api.value.PlcValue;
import org.connectorio.addons.binding.amsads.AmsAdsBindingConstants;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

/**
 * Handler which is responsible for controlling and discovering channel information.
 */
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

  ChannelTypeUID DATETIME_SYMBOL = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "datetime-symbol");
  ChannelTypeUID DATETIME_DIRECT_HEX = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "datetime-direct-hex");
  ChannelTypeUID DATETIME_DIRECT_DEC = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "datetime-direct-dec");

  ChannelTypeUID TEXT_SYMBOL = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "text-symbol");
  ChannelTypeUID TEXT_DIRECT_HEX = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "text-direct-hex");
  ChannelTypeUID TEXT_DIRECT_DEC = new ChannelTypeUID(AmsAdsBindingConstants.BINDING_ID, "text-direct-dec");

  Channel createChannel();

  AdsTag createTag();

  /**
   * Returns refresh interval time expressed in milliseconds if channel should be polled rather
   * than subscribed using ads notifications.
   *
   * @return
   */
  Long getRefreshInterval();

  void onChange(Object value);

  PlcValue update(Command command);
}
