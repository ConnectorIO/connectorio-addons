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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest.Builder;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.can.field.CANOpenPDOField;
import org.apache.plc4x.java.can.field.CANOpenSDOField;
import org.apache.plc4x.java.canopen.readwrite.IndexAddress;
import org.apache.plc4x.java.canopen.readwrite.io.IndexAddressIO;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.binding.plc4x.canopen.config.CANopenNodeConfig;
import org.connectorio.binding.plc4x.canopen.handler.CANopenBridgeHandler;
import org.connectorio.binding.plc4x.canopen.ta.internal.TACANopenBindingConstants;
import org.connectorio.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.binding.plc4x.canopen.ta.internal.handler.protocol.AbstractCallback;
import org.connectorio.binding.plc4x.canopen.ta.internal.handler.protocol.AnalogOutputCallback;
import org.connectorio.binding.plc4x.canopen.ta.internal.handler.protocol.DigitalOutputCallback;
import org.connectorio.binding.plc4x.shared.handler.Plc4xThingHandler;
import org.connectorio.binding.plc4x.shared.handler.base.PollingPlc4xThingHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
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

    updateStatus(ThingStatus.OFFLINE);

    getBridgeConnection().ifPresent(connection -> {
      try {
        logger.debug("Retrieving UVR {} configuration", nodeId);
        connection.subscriptionRequestBuilder().addEventField("tpdo_0x480", "TRANSMIT_PDO_4:" + nodeId + ":RECORD")
          .build().execute().get().getSubscriptionHandle("tpdo_0x480").register(this);

        ValueListener valueListener = new ValueListener() {
          @Override
          public void analog(int index, ReadBuffer value) throws ParseException {
            short val = value.readShort(16);
            Channel channel = getThing().getChannel("analog#" + index);
            logger.info("Analog channel {} (index {}) value {}", channel, index, val);
          }

          @Override
          public void digital(int index, boolean value) {
            Channel channel = getThing().getChannel("analog#" + index);
            logger.info("Digital channel {} (index {}) value {}", channel, index, value);
          }
        };

        subscribe(connection, "tpdo_0x180", "TRANSMIT_PDO_1:" + nodeId + ":RECORD", new DigitalOutputCallback(valueListener)); // digital
        subscribe(connection, "rpdo_0x200", "RECEIVE_PDO_1:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 0));
        subscribe(connection, "tpdo_0x280", "TRANSMIT_PDO_2:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 4));
        subscribe(connection, "rpdo_0x300", "RECEIVE_PDO_2:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 8));
        subscribe(connection, "tpdo_0x380", "TRANSMIT_PDO_3:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 12));
        subscribe(connection, "rpdo_0x240", "RECEIVE_PDO_1:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 16));
        subscribe(connection, "tpdo_0x2c0", "TRANSMIT_PDO_2:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 20));
        subscribe(connection, "rpdo_0x340", "RECEIVE_PDO_2:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 24));
        subscribe(connection, "tpdo_0x3c0", "TRANSMIT_PDO_3:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 28));
        subscribe(connection, "tpdo_0x3c0", "TRANSMIT_PDO_3:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 28));

        connection.writeRequestBuilder() // 0, CANOpenService.TRANSMIT_PDO_4, CANOpenDataType.RECORD
          .addItem("mpdo", "TRANSMIT_PDO_4:0:RECORD",
            //new CANOpenMPDO((short) (0x80 + nodeId), new IndexAddress(0x4e01, (short) 1), new byte[] {0,0,0,1})
            (Byte) (byte) (0x80 + nodeId),
            (Byte) (byte) 0x01,
            (Byte) (byte) 0x4e,
            (Byte) (byte) 0x01,
            (Byte) (byte) 0x01,
            (Byte) (byte) 0x00,
            (Byte) (byte) 0x00,
            (Byte) (byte) 0x00
          )
          .build().execute().get();
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
    final byte[] bytes = AbstractCallback.getBytes(event, event.getFieldNames().iterator().next());
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
        updateThingChannel(rawValue, unit, "analog#", 0x228f, subIndex, "Analog #", "Number:Dimensionless",
          TACANopenBindingConstants.ANALOG_OUTPUT_CHANNEL_TYPE);
      } else if (subIndex <= 64) { // digital
        final int digitalIndex = subIndex - 33;
        updateThingChannel(rawValue, unit, "digital#", 0x238f, digitalIndex, "Digital #", CoreItemFactory.CONTACT,
          TACANopenBindingConstants.DIGITAL_OUTPUT_CHANNEL_TYPE);
      }

    } catch (ParseException e) {
      logger.error("Could not parse configuration PDO", e);
    }
  }

  private void updateThingChannel(int rawValue, int unit, String channelId, int sdoIndex, int labelIndex, String fallbackLabel, String contact, ChannelTypeUID channelTypeUID) {
    ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId + labelIndex);

    String label = getBridgeConnection().map(connection -> {
      CANOpenSDOField field = new CANOpenSDOField(nodeId, (short) sdoIndex, (short) labelIndex, CANOpenDataType.VISIBLE_STRING);
      logger.info("Requesting data {}", field);
      return connection.readRequestBuilder()
        .addItem("label", field)
        .build().execute()
        .join()
        .getString("label");
    }).orElse(fallbackLabel + labelIndex);

    ThingBuilder thingBuilder = editThing();
    if (getThing().getChannel(channelUID) != null) {
      thingBuilder.withoutChannel(channelUID);
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("index", labelIndex);
    properties.put("unit", unit);
    Configuration configuration = new Configuration(properties);

    ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID, contact)
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

  private static void subscribe(PlcConnection connection, String name, String field, Consumer<PlcSubscriptionEvent> callback) throws ExecutionException, InterruptedException {
    Builder subscriptionBuilder = connection.subscriptionRequestBuilder();
    subscriptionBuilder.addEventField(name, field);

    PlcSubscriptionResponse response = subscriptionBuilder.build().execute().get();
    for (String subscriptionName : response.getFieldNames()) {
      response.getSubscriptionHandle(subscriptionName).register(callback);
    }
  }

}
