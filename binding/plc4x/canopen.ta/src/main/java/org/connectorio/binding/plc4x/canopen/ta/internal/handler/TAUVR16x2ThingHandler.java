/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.binding.plc4x.canopen.ta.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.canopen.readwrite.IndexAddress;
import org.apache.plc4x.java.canopen.readwrite.io.IndexAddressIO;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.binding.plc4x.canopen.config.CANopenNodeConfig;
import org.connectorio.binding.plc4x.canopen.handler.CANopenBridgeHandler;
import org.connectorio.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.binding.plc4x.canopen.ta.internal.handler.protocol.AbstractCallback;
import org.connectorio.binding.plc4x.shared.handler.Plc4xThingHandler;
import org.connectorio.binding.plc4x.shared.handler.base.PollingPlc4xThingHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TAUVR16x2ThingHandler extends PollingPlc4xThingHandler<PlcConnection, CANopenBridgeHandler<?>, CANopenNodeConfig>
  implements Plc4xThingHandler<PlcConnection, CANopenBridgeHandler<?>, CANopenNodeConfig>, Consumer<PlcSubscriptionEvent> {

  private final Logger logger = LoggerFactory.getLogger(TAUVR16x2ThingHandler.class);
  private int nodeId;

  public TAUVR16x2ThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    CANopenNodeConfig config = getConfigAs(CANopenNodeConfig.class);
    nodeId = config.nodeId;

    updateStatus(ThingStatus.INITIALIZING);

    getBridgeConnection().ifPresent(connection -> {
      try {
        connection.subscriptionRequestBuilder().addEventField("config", "TRANSMIT_PDO_4:" + nodeId + ":RECORD")
          .build().execute().get().getSubscriptionHandle("config").register(this);
        updateStatus(ThingStatus.ONLINE);
      } catch (InterruptedException | ExecutionException e) {
        logger.error("Could not initialize device handler", e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
      }
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

  @Override
  public void accept(PlcSubscriptionEvent event) {
    final byte[] bytes = AbstractCallback.getBytes(event, "config");
    try {
      ReadBuffer buffer = new ReadBuffer(bytes, true);
      int sender = buffer.readUnsignedShort(8);
      if (sender != nodeId) {
        logger.warn("Received configuration notification from wrong node: {}. Configured node id {}", sender, nodeId);
        return;
      }

      IndexAddress address = IndexAddressIO.staticParse(buffer);
      final int subIndex = address.getSubindex();
      int rawValue = buffer.readShort(16);
      buffer.readByte(8); // constant 0x41
      int unit = buffer.readUnsignedShort(8);

      if (subIndex <= 32) { // analog
        updateThingChannel(rawValue, unit, subIndex, "analog#", 0x228f, "Analog #", "Number:Dimensionless",
          TACANopenBindingConstants.ANALOG_OUTPUT_CHANNEL_TYPE);
      } else if (subIndex <= 64) { // digital
        final int digitalIndex = subIndex - 33;
        updateThingChannel(rawValue, unit, digitalIndex, "digital#", 0x238f, "Digital #", CoreItemFactory.CONTACT,
          TACANopenBindingConstants.DIGITAL_OUTPUT_CHANNEL_TYPE);
      }

    } catch (ParseException e) {
      logger.error("Could not parse configuration PDO", e);
    }
  }

  private void updateThingChannel(int rawValue, int unit, int labelIndex, String channelId, int index, String fallbackLabel,
    String itemType, ChannelTypeUID channelTypeUID) {

    ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId + labelIndex);

    String label = getBridgeConnection().map(connection ->
      connection.readRequestBuilder()
        .addItem("label", "SDO:" + nodeId + ":0x" + Integer.toHexString(labelIndex) + "/" + index + ":VISIBLE_STRING")
        .build().execute().join().getString("label")
    ).orElse(fallbackLabel + labelIndex);

    ThingBuilder thingBuilder = editThing();
    if (getThing().getChannel(channelUID) != null) {
      thingBuilder.withoutChannel(channelUID);
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("index", labelIndex);
    properties.put("unit", unit);
    Configuration configuration = new Configuration(properties);

    ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, itemType)
      .withKind(ChannelKind.STATE)
      .withLabel(label)
      .withType(channelTypeUID)
      .withConfiguration(configuration);
    thingBuilder.withChannel(channelBuilder.build());

    updateThing(thingBuilder.build());
    Optional.ofNullable(getCallback()).ifPresent(callback -> callback.stateUpdated(channelUID, createState(unit, rawValue)));
  }

  private State createState(int unitIndex, int rawValue) {
    if (unitIndex < 42) {
      final AnalogUnit unit = AnalogUnit.valueOf(unitIndex);
      final double value = rawValue * unit.getScale();
      return QuantityType.valueOf(value, unit.getUnit());
    }

    if (unitIndex < 47) {
      // TODO this logic requires additional verification
      DigitalUnit unit = DigitalUnit.valueOf(unitIndex);
      int value = Short.toUnsignedInt(Short.valueOf((short) unitIndex));
      return unit.parse(value);
    }

    // unknown
    return UnDefType.UNDEF;
  }

}
