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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.wmbus.WMBusBindingConstants;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageListener;
import org.connectorio.addons.binding.wmbus.internal.config.ChannelConfig;
import org.connectorio.addons.binding.wmbus.internal.config.DeviceConfig;
import org.connectorio.addons.binding.wmbus.internal.config.OpenHABSerialBridgeConfig;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.EncryptionMode;
import org.openmuc.jmbus.HexUtils;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.openmuc.jmbus.wireless.WMBusMessage;

public class WMBusDeviceThingHandler<B extends WMBusBridgeHandler<OpenHABSerialBridgeConfig>>
  extends GenericThingHandlerBase<B, DeviceConfig> implements WMBusMessageListener {

  private int serialNumber;
  private String manufacturerId;
  private Integer version;
  private DeviceType deviceType;

  private String encryptionKey;

  private boolean discoverChannels;

  private SecondaryAddress address;

  private final Map<ChannelKey, List<ChannelUID>> channelMap = new HashMap<>();

  public WMBusDeviceThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    DeviceConfig config = getConfigAs(DeviceConfig.class);

    serialNumber = config.serialNumber;
    manufacturerId = config.manufacturerId;
    version = config.version;
    deviceType = config.deviceType;
    discoverChannels = config.discoverChannels;
    encryptionKey = config.encryptionKey;

    if (Boolean.parseBoolean(getThing().getProperties().get(WMBusBindingConstants.THING_PROPERTY_ENCRYPTED)) && (encryptionKey == null || encryptionKey.trim().isEmpty())) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Provide encryption key.");
      return;
    }

    // create wm-bus address
    address = SecondaryAddress.newFromManufactureId(
      bcd(serialNumber),
      manufacturerId,
      version.byteValue(),
      Integer.valueOf(deviceType.getId()).byteValue(),
      false
    );

    if (encryptionKey != null) {
      getBridgeHandler().map(WMBusBridgeHandler::getKeyStore).orElse(CompletableFuture.failedFuture(new IllegalArgumentException()))
        .thenAccept(keyStore -> keyStore.addKey(address, HexUtils.hexToBytes(encryptionKey)));
    }

    for (Channel channel : getThing().getChannels()) {
      if (WMBusBindingConstants.CHANNEL_TYPE_RSSI.equals(channel.getChannelTypeUID())) {
        continue;
      }

      ChannelConfig channelConfig = channel.getConfiguration().as(ChannelConfig.class);
      ChannelKey channelKey = new ChannelKey(channelConfig.dib, channelConfig.vib);
      if (!channelMap.containsKey(channelKey)) {
        channelMap.put(channelKey, new ArrayList<>());
      }
      channelMap.get(channelKey).add(channel.getUID());
    }
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  protected State convertRecordData(DataRecord record) {
    switch (record.getDataValueType()) {
      case LONG:
      case DOUBLE:
      case BCD:
        return new DecimalType(record.getScaledDataValue());
      case DATE:
        return convertDate(record.getDataValue());
      case STRING:
      case NONE:
        return new StringType(record.getDataValue().toString());
    }
    return null;
  }

  private State convertDate(Object input) {
    if (input instanceof Date) {
      Date date = (Date) input;
      ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
      return new DateTimeType(zonedDateTime);
    }

    return UnDefType.NULL;
  }

  @Override
  public SecondaryAddress getAddress() {
    return address;
  }

  @Override
  public void onMessage(WMBusMessage message) {
    VariableDataStructure response = message.getVariableDataResponse();

    if (response.getEncryptionMode() != EncryptionMode.NONE) {
      if (encryptionKey == null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Device uses encrypted communication. Encryption key is missing, please update configuration.");
        return;
      }
      try {
        message.getVariableDataResponse().decode();
      } catch (DecodingException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Wrong encryption key - could not decrypt payload.");
        return;
      }
    }

    List<DataRecord> records = response.getDataRecords();
    if (discoverChannels) {
      // retrieve known channels to avoid overwriting existing or creating duplicate channel definitions
      List<ChannelUID> knownChannels = thing.getChannels().stream()
        .map(Channel::getUID)
        .collect(Collectors.toList());
      Set<Channel> newChannels = new LinkedHashSet<>();
      for (DataRecord record : records) {
        ChannelKey channelKey = new ChannelKey(record.getDib(), record.getVib());
        if (!channelMap.containsKey(channelKey)) {
          Channel channel = DataRecordChannelMapper.createChannel(getThing().getUID(), record);
          if (!knownChannels.contains(channel.getUID())) {
            newChannels.add(channel);
          }
        }
      }

      if (!newChannels.isEmpty()) {
        ThingBuilder thingBuilder = editThing();
        for (Channel channel : newChannels) {
          thingBuilder.withChannel(channel);
        }
        updateThing(thingBuilder.build());
      }
    }

    ThingHandlerCallback callback = getCallback();
    Integer rssi = message.getRssi();
    if (rssi != null) {
      callback.stateUpdated(new ChannelUID(getThing().getUID(), "rssi"), new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS));
    }

    // make sure we update status only when we receive message/frame
    // TODO port it to watchdog maybe?
    updateStatus(ThingStatus.ONLINE);

    for (DataRecord record : records) {
      ChannelKey channelKey = new ChannelKey(record.getDib(), record.getVib());
      List<ChannelUID> channels = channelMap.get(channelKey);
      if (channels != null) {
        for (ChannelUID channel : channels) {
          callback.stateUpdated(channel, convertRecordData(record));
        }
      }
    }
  }

  private static byte[] bcd(int value){
    byte[] bcd = new byte[4];
    for (int index = 0; index < 4; index++){
      bcd[index] = (byte) (value % 10);
      value /= 10;
      bcd[index] |= (byte) ((value % 10) << 4);
      value /= 10;
    }
    return bcd;
  }
}
