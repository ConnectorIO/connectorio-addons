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
package org.connectorio.addons.binding.wmbus.internal.handler;

import java.util.Map;
import java.util.Map.Entry;
import javax.measure.Unit;
import org.connectorio.addons.binding.wmbus.WMBusBindingConstants;
import org.connectorio.addons.binding.wmbus.internal.unit.DlmsUnits;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DataRecord.DataValueType;
import org.openmuc.jmbus.DataRecord.FunctionField;
import org.openmuc.jmbus.HexUtils;

public class DataRecordChannelMapper {

  public static Channel createChannel(ThingUID thingUID, DataRecord record) {
    Unit<?> unit = DlmsUnits.valueOf(record.getUnit());
    Entry<ChannelTypeUID, String> itemType = determineChannelType(record.getDataValueType(), unit);

    ChannelKey channelKey = new ChannelKey(record.getDib(), record.getVib());
    ChannelUID channelUID = new ChannelUID(thingUID, channelKey.asString());
    Configuration configuration = new Configuration(Map.of(
      "dib", HexUtils.bytesToHex(record.getDib()),
      "vib",  HexUtils.bytesToHex(record.getVib())
    ));

    String label = getFunction(record.getFunctionField()) + " ";
    label += record.getDescription().name().toLowerCase().replace("_", " ");
    if (record.getTariff() != 0) {
      label += " tariff " + record.getTariff();
    }
    if (record.getStorageNumber() != 0) {
      label += " storage " + record.getStorageNumber();
    }

    return ChannelBuilder.create(channelUID, itemType.getValue())
      .withKind(ChannelKind.STATE)
      .withType(itemType.getKey())
      .withLabel(label)
      .withConfiguration(configuration)
      .build();
  }

  private static Entry<ChannelTypeUID, String> determineChannelType(DataValueType valueType, Unit<?> unit) {
    switch (valueType) {
      case BCD:
      case DOUBLE:
      case LONG:
        return Map.entry(WMBusBindingConstants.CHANNEL_TYPE_NUMBER, CoreItemFactory.NUMBER);
      case DATE:
        return Map.entry(WMBusBindingConstants.CHANNEL_TYPE_DATETIME, CoreItemFactory.DATETIME);
    }

    return Map.entry(WMBusBindingConstants.CHANNEL_TYPE_STRING, CoreItemFactory.STRING);
  }

  private static String getFunction(FunctionField function) {
    switch (function) {
      case ERROR_VAL:
        return "Error";
      case INST_VAL:
        return "Present";
      case MAX_VAL:
        return "Maximum";
      case MIN_VAL:
        return "Minimum";

    }

    return "Unknown";
  }
}
