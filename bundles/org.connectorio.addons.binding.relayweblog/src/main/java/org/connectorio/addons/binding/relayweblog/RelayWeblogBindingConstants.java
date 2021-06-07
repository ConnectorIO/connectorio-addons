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
package org.connectorio.addons.binding.relayweblog;

import javax.measure.Quantity;
import org.connectorio.addons.binding.BaseBindingConstants;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

public interface RelayWeblogBindingConstants extends BaseBindingConstants {

  String BINDING_ID = BaseBindingConstants.identifier("relayweblog");

  String WEBLOG_THING = "weblog";
  String METER_THING = "meter";
  String SUB_METER_THING = "sub-meter";

  ThingTypeUID WEBLOG_THING_TYPE = new ThingTypeUID(BINDING_ID, WEBLOG_THING);
  ThingTypeUID METER_THING_TYPE = new ThingTypeUID(BINDING_ID, METER_THING);
  ThingTypeUID SUB_METER_THING_TYPE = new ThingTypeUID(BINDING_ID, SUB_METER_THING);

  // five minutes
  Long DEFAULT_POLLING_INTERVAL = 300_000L;

  String DATE_TIME = "datetime";
  String STATUS = "status";
  ChannelTypeUID DATE_TIME_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, DATE_TIME);
  ChannelTypeUID STATUS_CHANNEL_TYPE = new ChannelTypeUID(BINDING_ID, STATUS);

  static ChannelTypeUID channelType(Class<? extends Quantity<?>> type) {
    return new ChannelTypeUID(BINDING_ID, type.getSimpleName().toLowerCase());
  }

  String ENHANCED_IDENTIFICATION_FIELD = "(Enhanced) identification";
  String FABRICATION_IDENTIFIER_FIELD = "Fabrication number";
  String DATE_AND_TIME_FIELD = "Date and time";
  String ERROR_FLAGS_FIELD = "Error flags";


  // custom thing properties
  String PROPERTY_METER_ID = "id";
  String PROPERTY_METER_IDENTIFIER = "meter.identifier";
  String PROPERTY_METER_MEDIUM = "meter.medium";
  String PROPERTY_METER_INDEX = "meter.index";

  String PROPERTY_PARENT_ID = "parentId";
  String PROPERTY_PARENT_IDENTIFIER = "parentIdentifier";
  String PROPERTY_PARENT_SERIAL_NUMBER = "parentSerialNumber";

}
