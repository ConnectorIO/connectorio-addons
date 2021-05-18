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
package org.connectorio.addons.binding.plc4x.canopen.internal.provider;

import java.util.Arrays;
import java.util.List;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.openhab.core.thing.type.ChannelTypeUID;

abstract class BaseChannelTypeProvider {

  protected static final List<ChannelTypeDef> ENTRIES = Arrays.asList(
    new ChannelTypeDef(CANOpenDataType.BOOLEAN, "%d", "Switch"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED8, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED16, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED24, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED32, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED40, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED48, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED56, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.UNSIGNED64, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER8, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER16, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER24, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER32, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER40, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER48, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER56, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.INTEGER64, "%d", "Number"),
    new ChannelTypeDef(CANOpenDataType.REAL32, "%.2f", "Number"),
    new ChannelTypeDef(CANOpenDataType.REAL64, "%.2f", "Number"),
    new ChannelTypeDef(CANOpenDataType.RECORD, "%.2f", "Number"),
    new ChannelTypeDef(CANOpenDataType.OCTET_STRING, "%s", "String"),
    new ChannelTypeDef(CANOpenDataType.VISIBLE_STRING, "%s", "String"),
    new ChannelTypeDef(CANOpenDataType.UNICODE_STRING, "%s", "String"),
    new ChannelTypeDef(CANOpenDataType.TIME_OF_DAY, "%s", "DateTime"),
    new ChannelTypeDef(CANOpenDataType.TIME_DIFFERENCE, "%s", "DateTime")
  );

  protected static String createLabel(CANOpenDataType type) {
    String name = type.name().toLowerCase().replace("_", " ");
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  public static CANOpenDataType typeFromChannel(ChannelTypeUID type) {
    try {
      String id = type.getId();
      return CANOpenDataType.valueOf(id.substring(id.lastIndexOf('-') + 1).toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
