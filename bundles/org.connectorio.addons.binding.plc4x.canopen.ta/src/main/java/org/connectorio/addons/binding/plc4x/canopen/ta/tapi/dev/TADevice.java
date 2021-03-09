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
package org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.values.PlcSINT;
import org.apache.plc4x.java.spi.values.PlcUSINT;
import org.apache.plc4x.java.spi.values.PlcValues;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.TACanString;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.func.BasicFunction;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.func.TAFunction;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.AnalogGroup;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalInput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.io.TADigitalOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.AnalogValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.DigitalValue;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TADevice {

  private final Logger logger = LoggerFactory.getLogger(TADevice.class);

  private final ObjectFactory objectFactory = new DefaultObjectFactory(this);
  private final CoNode node;
  private final int clientId;
  private final int maxFunctions;
  private final int physicalInput;
  private final int physicalOutput;
  private final Map<Integer, TAFunction> functions = new ConcurrentHashMap<>();
  private final Map<Integer, TAAnalogOutput> analogOutputs = new ConcurrentHashMap<>();
  private final Map<Integer, TADigitalOutput> digitalOutputs = new ConcurrentHashMap<>();
  private final Map<Integer, TAAnalogInput> analogInput = new ConcurrentHashMap<>();
  private final Map<Integer, TADigitalInput> digitalInput = new ConcurrentHashMap<>();
  private final List<Consumer<Boolean>> statusCallbacks = new CopyOnWriteArrayList<>();
  private final List<ValueCallback> valueCallbacks = new CopyOnWriteArrayList<>();
  private final List<InOutCallback> inOutCallbacks = new CopyOnWriteArrayList<>();

  private final TACanString name;
  private /*final*/ TACanString function;
  private /*final*/ TACanString version;
  private /*final*/ TACanString serial;
  private /*final*/ TACanString productionDate;
  private /*final*/ TACanString bootsector;
  private /*final*/ TACanString hardwareCover;
  private /*final*/ TACanString hardwareMains;

  public TADevice(CoNode node, int clientId, int functions, int physicalInputLimit, int physicalOutputLimit) {
    this.node = node;
    this.clientId = clientId;
    this.maxFunctions = functions;
    this.physicalInput = physicalInputLimit;
    this.physicalOutput = physicalOutputLimit;

    subscribe(node.getNodeId(), CANOpenService.RECEIVE_PDO_1, new AnalogOutputCallback(this, 0));
    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_2, new AnalogOutputCallback(this, 4));
    subscribe(node.getNodeId(), CANOpenService.RECEIVE_PDO_2, new AnalogOutputCallback(this, 8));
    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_3, new AnalogOutputCallback(this, 12));
    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_1, new DigitalOutputCallback(this)); // digital

    subscribe(node.getNodeId() + 0x40, CANOpenService.RECEIVE_PDO_1, new AnalogOutputCallback(this, 16));
    subscribe(node.getNodeId() + 0x40, CANOpenService.TRANSMIT_PDO_2, new AnalogOutputCallback(this, 20));
    subscribe(node.getNodeId() + 0x40, CANOpenService.RECEIVE_PDO_2, new AnalogOutputCallback(this, 24));
    subscribe(node.getNodeId() + 0x40, CANOpenService.TRANSMIT_PDO_3, new AnalogOutputCallback(this, 28));

    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_4, new ConfigurationCallback(this, objectFactory));
    subscribe(node.getNodeId(), CANOpenService.RECEIVE_PDO_3, new StatusCallback(clientId, new Consumer<Boolean>() {
      @Override
      public void accept(Boolean loggedIn) {
        statusCallbacks.forEach(callback -> callback.accept(loggedIn));
      }
    }));

    name = new TACanString(node, (short) 0x2512, (short) 0x00);
//    function = new TACanString(node, (short) 0x57E0, (short) 0x07);
//    version = new TACanString(node, (short) 0x57E0, (short) 0x00);
//    serial = new TACanString(node, (short) 0x57E0, (short) 0x01);
//    productionDate = new TACanString(node, (short) 0x57E0, (short) 0x02);
//    bootsector = new TACanString(node, (short) 0x57E0, (short) 0x03);
//    hardwareCover = new TACanString(node, (short) 0x57E0, (short) 0x04);
//    hardwareMains = new TACanString(node, (short) 0x57E0, (short) 0x05);
  }


  public Optional<String> getName() {
    return Optional.ofNullable(name.get());
  }
  public Optional<String> getFunction() {
    return Optional.ofNullable(function.get());
  }
  public Optional<String> getVersion() {
    return Optional.ofNullable(version.get());
  }
  public Optional<String> getSerial() {
    return Optional.ofNullable(serial.get());
  }
  public Optional<String> getProductionDate() {
    return Optional.ofNullable(productionDate.get());
  }
  public Optional<String> getBootsector() {
    return Optional.ofNullable(bootsector.get());
  }
  public Optional<String> getHardwareCover() {
    return Optional.ofNullable(hardwareCover.get());
  }
  public Optional<String> getHardwareMains() {
    return Optional.ofNullable(hardwareMains.get());
  }

  public void addValueCallback(ValueCallback<?> callback) {
    valueCallbacks.add(callback);
  }

  public void removeValueCallback(ValueCallback<?> callback) {
    valueCallbacks.remove(callback);
  }

  public void addStatusCallback(Consumer<Boolean> callback) {
    statusCallbacks.add(callback);
  }

  public void removeStatusCallback(Consumer<Boolean> callback) {
    statusCallbacks.remove(callback);
  }

  public void addInOutCallback(InOutCallback callback) {
    inOutCallbacks.add(callback);
  }

  public void removeInOutCallback(InOutCallback callback) {
    inOutCallbacks.remove(callback);
  }

  public void write(int index, Value<?> value) {
    int writeIndex = 0;
    if (value instanceof AnalogValue) {
      analogInput.get(index).update((AnalogValue) value);
      try {
        WriteBuffer buffer = new WriteBuffer(8);

        AnalogGroup analogGroup = new AnalogGroup(node.getNodeId(), index);
        logger.debug("Sending update of analog input {} with neighbors - from {} to {}", index, analogGroup.getStartBoundary(), analogGroup.getEndBoundary());

        for (int objectIndex = analogGroup.getStartBoundary(); objectIndex < analogGroup.getEndBoundary(); objectIndex++) {
          TAAnalogInput input = analogInput.get(objectIndex);
          if (input == null) {
            logger.debug("Uninitialized input {}. assuming empty value", objectIndex);
            buffer.writeInt(16, 0);
            continue;
          }

          buffer.writeShort(16, input.getValue().encode());
          writeIndex++;
        }

        send(analogGroup.getNodeId(), analogGroup.getService(), buffer);
      } catch (ParseException e) {
        logger.error("Failed to update analog input {}, failure at index {}", index, writeIndex, e);
      }
    } else if (value instanceof DigitalValue) {
      digitalInput.get(index).update((DigitalValue) value);
      try {
        WriteBuffer buffer = new WriteBuffer(8);
        for (TADigitalInput input : digitalInput.values()) {
          buffer.writeBit(input.getValue().getValue());
          writeIndex++;
        }
        for (int padding = writeIndex; padding <= 64; padding++) {
          buffer.writeBit(false);
          writeIndex++;
        }

        send(node.getNodeId(), CANOpenService.TRANSMIT_PDO_1, buffer);
      } catch (ParseException e) {
        logger.error("Failed to update digital input {}, failure at index {}", index, writeIndex, e);
      }
    } else {
      logger.error("Unsupported write value {} {}", index, value);
    }
  }

  private void send(int nodeId, CANOpenService service, WriteBuffer buffer) {
    byte[] data = buffer.getData();
    node.getConnection().send(nodeId, service, PlcValues.of(
      new PlcSINT(data[0]), new PlcSINT(data[1]), new PlcSINT(data[2]), new PlcSINT(data[3]),
      new PlcSINT(data[4]), new PlcSINT(data[5]), new PlcSINT(data[6]), new PlcSINT(data[7])
    ));
  }

  public void reload() {
    reloadOutputs();
    //scanFunctions();
  }

  public void login() {
    node.getConnection().send(clientId, CANOpenService.RECEIVE_PDO_3, PlcValues.of(
      new PlcUSINT(0x80 + node.getNodeId()),
      new PlcUSINT(0x00),
      new PlcUSINT(0x1F),
      new PlcUSINT(0x00),
      new PlcUSINT(node.getNodeId()),
      new PlcUSINT(clientId),
      new PlcUSINT(0x80),
      new PlcUSINT(0x12)
    ));
  }

  public void logout() {
    node.getConnection().send(clientId, CANOpenService.RECEIVE_PDO_3, PlcValues.of(
      new PlcUSINT(0x80 + node.getNodeId()),
      new PlcUSINT(0x01),
      new PlcUSINT(0x1F),
      new PlcUSINT(0x00),
      new PlcUSINT(node.getNodeId()),
      new PlcUSINT(clientId),
      new PlcUSINT(0x80),
      new PlcUSINT(0x12)
    ));
  }

  public void close() {
    node.close();
  }

  public int getMaxFunctions() {
    return maxFunctions;
  }

  public int getPhysicalInput() {
    return physicalInput;
  }

  public int getPhysicalOutput() {
    return physicalOutput;
  }

  public Map<Integer, TAFunction> getFunctions() {
    return functions;
  }

  public CoNode getNode() {
    return node;
  }

  void updateAnalog(int index, short value) {
    logger.trace("Received update of analog output {} with value {} (0x{})", index, value, Integer.toHexString(value));

    if (!analogInput.containsKey(index)) {
      logger.trace("Lazy initialization of analog output {} with unknown unit", index);
      analogOutputs.put(index, objectFactory.createAnalogOutput(index, -1, value));
    }

    TAAnalogOutput analogOutput = analogOutputs.get(index);
    analogOutput.update(value);
    valueCallbacks.forEach(callback -> callback.accept(index, analogOutput.getValue()));
  }

  void updateDigital(int index, boolean value) {
    logger.trace("Received update of digital output {} with value {}", index, value);

    if (!digitalOutputs.containsKey(index)) {
      logger.trace("Lazy initialization of digital output {} with unknown unit", index);
      digitalOutputs.put(index, objectFactory.createDigitalOutput(index, -1, value));
    }

    TADigitalOutput digitalOutput = digitalOutputs.get(index);
    digitalOutput.update(value);
    valueCallbacks.forEach(callback -> callback.accept(index, digitalOutput.getValue()));
  }

  public void addAnalogOutput(int index, TAUnit unit) {
    if (analogOutputs.containsKey(index)) {
      TAAnalogOutput base = analogOutputs.get(index);
      if (base.getUnit() != unit.getIndex() && base.getUnit() == AnalogUnit.DIMENSIONLESS.getIndex()) {
        analogOutputs.put(index, new TAAnalogOutput(this, false, base.getIndex(), unit.getIndex(),  (short) 0));
        logger.debug("Update of analog output {} unit from {} to {}", index, base.getUnit(), unit.getIndex());
      } else {
        logger.debug("Skipping update of analog output {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      analogOutputs.put(index, new TAAnalogOutput(this, false, index, unit.getIndex(),  (short) 0));
    }
  }

  public void addAnalogInput(int index, TAUnit unit) {
    if (analogInput.containsKey(index)) {
      TAAnalogInput base = analogInput.get(index);
      if (base.getUnit() != unit.getIndex() && base.getUnit() == AnalogUnit.DIMENSIONLESS.getIndex()) {
        analogInput.put(index, new TAAnalogInput(this, false, base.getIndex(), unit.getIndex(),  (short) 0));
        logger.debug("Update of analog input {} unit from {} to {}", index, base.getUnit(), unit.getIndex());
      } else {
        logger.debug("Skipping update of analog input {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      analogInput.put(index, new TAAnalogInput(this, false, index, unit.getIndex(),  (short) 0));
    }
  }

  public void addDigitalOutput(int index, TAUnit unit) {
    if (digitalOutputs.containsKey(index)) {
      TADigitalOutput base = digitalOutputs.get(index);
      if (base.getUnit() != unit.getIndex() && base.getUnit() == DigitalUnit.OPEN_CLOSED.getIndex()) {
        digitalOutputs.put(index, new TADigitalOutput(this, false, base.getIndex(), unit.getIndex(), false));
        logger.debug("Update of digital output {} unit from {} to {}", index, base.getUnit(), unit.getIndex());
      } else {
        logger.debug("Skipping update of digital output {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      digitalOutputs.put(index, new TADigitalOutput(this, false, index, unit.getIndex(), false));
    }
  }

  public void addDigitalInput(int index, TAUnit unit) {
    if (digitalInput.containsKey(index)) {
      TADigitalInput base = digitalInput.get(index);
      if (base.getUnit() != unit.getIndex() && base.getUnit() == DigitalUnit.OPEN_CLOSED.getIndex()) {
        digitalInput.put(index, new TADigitalInput(this, false, base.getIndex(), unit.getIndex(),  (short) 0));
        logger.debug("Update of digital input {} unit from {} to {}", index, base.getUnit(), unit.getIndex());
      } else {
        logger.debug("Skipping update of digital input {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      digitalInput.put(index, new TADigitalInput(this, false, index, unit.getIndex(),  (short) 0));
    }
  }

  void discoverDigitalOutput(TADigitalOutput output) {
    digitalOutputs.put(output.getIndex(), output);
    logger.info("Discovered new digital output {}", output);
    inOutCallbacks.forEach(callback -> callback.accept(output));
  }

  void discoverAnalogOutput(TAAnalogOutput output) {
    analogOutputs.put(output.getIndex(), output);
    logger.info("Discovered new analog output {}", output);
    inOutCallbacks.forEach(callback -> callback.accept(output));
  }

  protected void subscribe(int nodeId, CANOpenService service, Consumer<byte[]> consumer) {
    node.getConnection().subscribe(nodeId, service, consumer).whenComplete((sub, err) -> {
      if (err == null) {
        logger.info("Successfully subscribed to notifications from COB {} (node {})", Integer.toHexString(service.getMin() + nodeId), nodeId);
      }
    });
  }

  private void reloadOutputs() {
    node.getConnection().send(0, CANOpenService.TRANSMIT_PDO_4, PlcValues.of(
      new PlcUSINT(0x80 + node.getNodeId()),
      new PlcUSINT(0x01),
      new PlcUSINT(0x4e),
      new PlcUSINT(0x01),
      new PlcUSINT(0x01),
      new PlcUSINT(0x00),
      new PlcUSINT(0x00),
      new PlcUSINT(0x00)
    ));
  }

  private void scanFunctions() {
    for (short index = 0; index <= maxFunctions; index++) {
      int functionIndex = index;
      node.read((short) 0x4800, index).whenComplete((response, error) -> {
        byte type = response[1];
        if (type == 0x00) {
          functions.remove(functionIndex);
        } else {
          functions.put(functionIndex, new BasicFunction(this, functionIndex + 1));
        }
      });
    }
  }

  @Override
  public String toString() {
    return "TADevice [" + node + ", " + name + "]";
  }

}
