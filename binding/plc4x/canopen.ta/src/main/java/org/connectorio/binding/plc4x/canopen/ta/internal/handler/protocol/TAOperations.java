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
package org.connectorio.binding.plc4x.canopen.ta.internal.handler.protocol;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionField;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.can.field.CANOpenSDOField;
import org.apache.plc4x.java.canopen.readwrite.IndexAddress;
import org.apache.plc4x.java.canopen.readwrite.io.IndexAddressIO;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.apache.plc4x.java.spi.values.PlcUSINT;
import org.apache.plc4x.java.spi.values.PlcValues;
import org.connectorio.binding.plc4x.canopen.ta.internal.handler.ValueListener;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAADigitalOutput;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAAnalogOutput;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAObject;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAString;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TAOperations {

  private static final UncaughtExceptionHandler ERROR_HANDLER = new UncaughtExceptionHandler() {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void uncaughtException(Thread thread, Throwable error) {
      logger.error("Unhandled error occurred in canopen-ta-events thread {}", thread, error);
    }
  };

  private static final ExecutorService EVENTS = Executors.newCachedThreadPool((runnable) -> {
    Thread thread = new Thread(runnable, "canopen-ta-events");
    thread.setUncaughtExceptionHandler(ERROR_HANDLER);
    return thread;
  });
  private final Logger logger = LoggerFactory.getLogger(TAOperations.class);
  private final PlcConnection connection;
  private final List<PlcConsumerRegistration> registrations = new CopyOnWriteArrayList<>();

  public TAOperations(PlcConnection connection) {
    this.connection = connection;
  }

  public CompletableFuture<Map<String, String>> identify(int nodeId) {
    return connection.readRequestBuilder()
      .addItem("type", new CANOpenSDOField(nodeId, (short) 0x23E2, (short) 0x01, CANOpenDataType.UNSIGNED8))
      .build().execute().thenCompose((response) -> {
        if (response.getInteger("type") == 0x87) {
          return identifyUVR16x2(nodeId);
        }

        return CompletableFuture.completedFuture(Collections.emptyMap());
      });
  }

  protected CompletableFuture<Map<String, String>> identifyUVR16x2(int nodeId) {
    CompletableFuture<? extends PlcReadResponse> request = connection.readRequestBuilder()
      .addItem("name", new CANOpenSDOField(nodeId, (short) 0x2512, (short) 0x00, CANOpenDataType.RECORD))
      .addItem("function", new CANOpenSDOField(nodeId, (short) 0x57E0, (short) 0x07, CANOpenDataType.RECORD))
      .addItem("version", new CANOpenSDOField(nodeId, (short) 0x57E0, (short) 0x00, CANOpenDataType.RECORD))
      .addItem("serial", new CANOpenSDOField(nodeId, (short) 0x57E0, (short) 0x01, CANOpenDataType.RECORD))
      .addItem("production_date", new CANOpenSDOField(nodeId, (short) 0x57E0, (short) 0x02, CANOpenDataType.RECORD))
      .addItem("bootsector", new CANOpenSDOField(nodeId, (short) 0x57E0, (short) 0x03, CANOpenDataType.RECORD))
      .addItem("hardware_cover", new CANOpenSDOField(nodeId, (short) 0x57E0, (short) 0x04, CANOpenDataType.RECORD))
      .addItem("hardware_mains", new CANOpenSDOField(nodeId, (short) 0x57E0, (short) 0x05, CANOpenDataType.RECORD))
      .build().execute();

    return request.thenApply(response -> {
      Map<String, String> fields = new LinkedHashMap<>();

      readStringField(response, "name", (value) -> fields.put("name", value));
      readStringField(response, "function", (value) -> fields.put("function", value));
      readStringField(response, "version", (value) -> fields.put("version", value));
      readStringField(response, "serial", (value) -> fields.put("serial", value));
      readStringField(response, "production_date", (value) -> fields.put("production_date", value));
      readStringField(response, "bootsector", (value) -> fields.put("bootsector", value));
      readStringField(response, "hardware_cover", (value) -> fields.put("hardware_cover", value));
      readStringField(response, "hardware_mains", (value) -> fields.put("hardware_mains", value));

      return fields;
    });
  }

  private void readStringField(PlcReadResponse response, String name, Consumer<String> consumer) {
    if (response.getResponseCode(name) == PlcResponseCode.OK) {
      Optional.of(AbstractCallback.getBytes(response, name)).map(TAString::new).map(Objects::toString).ifPresent(consumer);
    }
  }

  public CompletableFuture<?> reload(int nodeId) {
    return connection.writeRequestBuilder().addItem("mpdo", "TRANSMIT_PDO_4:0:RECORD",
      (Byte) (byte) (0x80 + nodeId),
      (Byte) (byte) 0x01,
      (Byte) (byte) 0x4e,
      (Byte) (byte) 0x01,
      (Byte) (byte) 0x01,
      (Byte) (byte) 0x00,
      (Byte) (byte) 0x00,
      (Byte) (byte) 0x00
    ).build().execute().whenComplete((response, error) -> {
      logger.debug("Dispatched node {} reload request", nodeId, error);
    });
  }

  public CompletableFuture<? extends PlcWriteResponse> login(int nodeId, int clientId) {
    return connection.writeRequestBuilder().addItem("mpdo", "RECEIVE_PDO_3:" + clientId + ":RECORD", PlcValues.of(
      new PlcUSINT((0x80 + nodeId)),
      new PlcUSINT(0x00),
      new PlcUSINT(0x1F),
      new PlcUSINT(0x00),
      new PlcUSINT(nodeId),
      new PlcUSINT(clientId),
      new PlcUSINT(0x80),
      new PlcUSINT(0x12)
    )).build().execute().whenComplete((response, error) -> {
      logger.debug("Dispatched login request to node {}", nodeId, error);
    });
  }

  public CompletableFuture<? extends PlcWriteResponse> logout(int nodeId, int clientId) {
    return connection.writeRequestBuilder().addItem("mpdo", "RECEIVE_PDO_3:" + clientId + ":RECORD", PlcValues.of(
      new PlcUSINT((0x80 + nodeId)),
      new PlcUSINT(0x01),
      new PlcUSINT(0x1F),
      new PlcUSINT(0x00),
      new PlcUSINT(nodeId),
      new PlcUSINT(clientId),
      new PlcUSINT(0x80),
      new PlcUSINT(0x12)
    )).build().execute().whenComplete((response, error) -> {
      logger.debug("Dispatched logout request to node {}", nodeId, error);
    });
  }

  public void subscribeInputOutputState(ValueListener valueListener, int nodeId) {
    subscribe("tpdo_0x180", "TRANSMIT_PDO_1:" + nodeId + ":RECORD", new DigitalOutputCallback(valueListener)); // digital
    subscribe("rpdo_0x200", "RECEIVE_PDO_1:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 0));
    subscribe("tpdo_0x280", "TRANSMIT_PDO_2:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 4));
    subscribe("rpdo_0x300", "RECEIVE_PDO_2:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 8));
    subscribe("tpdo_0x380", "TRANSMIT_PDO_3:" + nodeId + ":RECORD", new AnalogOutputCallback(valueListener, 12));
    subscribe("rpdo_0x240", "RECEIVE_PDO_1:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 16));
    subscribe("tpdo_0x2c0", "TRANSMIT_PDO_2:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 20));
    subscribe("rpdo_0x340", "RECEIVE_PDO_2:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 24));
    subscribe("tpdo_0x3c0", "TRANSMIT_PDO_3:" + nodeId + 0x40 + ":RECORD", new AnalogOutputCallback(valueListener, 28));
  }

  public void subscribeStatus(Consumer<Boolean> connected, int nodeId, int clientId) {
    subscribe("status", "RECEIVE_PDO_3:" + nodeId + ":RECORD", new StatusCallback(connected, nodeId, clientId));
  }

  private void subscribe(String name, String field, Consumer<PlcSubscriptionEvent> callback) {
    logger.debug("Subscribe to event {} with mask {}", name, field);

    connection.subscriptionRequestBuilder().addEventField(name, field)
      .build().execute().whenComplete(new SubscriptionResponseCallback(EVENTS, registrations, callback));
  }

  public void subscribeInputOutputConfig(Consumer<TAObject> consumer, int nodeId) {
    ConfigCallback callback = new ConfigCallback(connection, consumer, nodeId);

    connection.subscriptionRequestBuilder().addEventField("config", "TRANSMIT_PDO_4:" + nodeId + ":RECORD")
      .build().execute().whenComplete(new SubscriptionResponseCallback(EVENTS, registrations, callback));

  }

  public void close() {
    for (PlcConsumerRegistration registration : registrations) {
      registration.unregister();
    }
  }

//  String label = getBridgeConnection().map(connection -> {
//    CANOpenSDOField field = new CANOpenSDOField(nodeId, (short) sdoIndex, (short) labelIndex, CANOpenDataType.VISIBLE_STRING);
//    logger.info("Requesting data {}", field);
//    return connection.readRequestBuilder()
//      .addItem("label", field)
//      .build().execute()
//      .join()
//      .getString("label");
//  }).orElse(fallbackLabel + labelIndex);

  static class ConfigCallback implements Consumer<PlcSubscriptionEvent> {

    private final Logger logger = LoggerFactory.getLogger(ConfigCallback.class);
    private final PlcConnection connection;
    private final Consumer<TAObject> consumer;
    private final int nodeId;

    public ConfigCallback(PlcConnection connection, Consumer<TAObject> consumer, int nodeId) {
      this.connection = connection;
      this.consumer = consumer;
      this.nodeId = nodeId;
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
        short rawValue = buffer.readShort(16);
        buffer.readByte(8); // constant 0x41
        int unit = buffer.readUnsignedShort(8);

        TAValue value = new TAValue(unit, rawValue);
        if (logger.isDebugEnabled()) {
          logger.debug("Received IO configuration from node: {}. Data {}, sub index: {}, raw {}, unit {}, converted to value {}.", sender, Hex.encodeHexString(bytes),
            subIndex, rawValue, unit, value.getValue());
        }

        if (subIndex <= 32) { // analog
          TAAnalogOutput output = new TAAnalogOutput(subIndex, unit);
          output.setValue(value);
          readLabel(nodeId, output.getLabelAddress()).exceptionally((error) -> {
            logger.error("Could not retrieve {} label", output, error);
            return output.toString();
          }).thenAccept(output::setLabel).thenAccept(label -> consumer.accept(output));
        } else if (subIndex <= 64) { // digital
          TAADigitalOutput output = new TAADigitalOutput(subIndex - 32, unit);
          output.setValue(value);
          readLabel(nodeId, output.getLabelAddress()).exceptionally((error) -> {
            logger.error("Could not retrieve {} label", output, error);
            return output.toString();
          }).thenAccept(output::setLabel).thenAccept(label -> consumer.accept(output));
        } else {
          logger.warn("Value {} out of range", subIndex);
        }

      } catch (ParseException e) {
        logger.error("Could not parse configuration PDO", e);
      }
    }

    private CompletableFuture<String> readLabel(int nodeId, IndexAddress labelAddress) {
      CANOpenSDOField field = new CANOpenSDOField(nodeId, (short) labelAddress.getIndex(), labelAddress.getSubindex(),
        CANOpenDataType.RECORD);

      return connection.readRequestBuilder().addItem("label", field)
        .build().execute().thenApply(response -> {
          byte[] label = AbstractCallback.getBytes(response, "label");
          return new TAString(label).getValue();
        });
    }
}

static class SubscriptionResponseCallback implements BiConsumer<PlcSubscriptionResponse, Throwable> {

  private final Logger logger = LoggerFactory.getLogger(SubscriptionResponseCallback.class);
  private final ExecutorService executor;
  private final Consumer<PlcSubscriptionEvent> callback;
  private final List<PlcConsumerRegistration> registrations;

  SubscriptionResponseCallback(ExecutorService executor,
    List<PlcConsumerRegistration> registrations, Consumer<PlcSubscriptionEvent> callback) {
    this.executor = executor;
    this.registrations = registrations;
    this.callback = callback;
  }

  @Override
  public void accept(PlcSubscriptionResponse response, Throwable throwable) {
    if (throwable != null) {
      logger.warn("Could not complete subscribe request", throwable);
      return;
    }
    for (String subscriptionName : response.getFieldNames()) {
      PlcSubscriptionField field = response.getRequest().getField(subscriptionName);
      PlcResponseCode responseCode = response.getResponseCode(subscriptionName);
      if (responseCode == PlcResponseCode.OK) {
        registrations.add(response.getSubscriptionHandle(subscriptionName).register(new Consumer<PlcSubscriptionEvent>() {
          @Override
          public void accept(PlcSubscriptionEvent event) {
            executor.submit(new Runnable() {
              @Override
              public void run() {
                logger.debug("Dispatching event with fields {}", event.getFieldNames());
                callback.accept(event);
              }
            });
          }
        }));
        logger.info("Successfully subscribed to field {}", subscriptionName);
      } else {
        logger.info("Failed to subscribed to field {}. Returned code: {}", field, responseCode);
      }
      }
    }
  }
}
