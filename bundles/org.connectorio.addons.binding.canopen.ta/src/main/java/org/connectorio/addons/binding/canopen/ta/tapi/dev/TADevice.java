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
package org.connectorio.addons.binding.canopen.ta.tapi.dev;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.codec.binary.Hex;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.values.PlcSINT;
import org.apache.plc4x.java.spi.values.PlcUSINT;
import org.apache.plc4x.java.spi.values.PlcValues;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.canopen.ta.internal.handler.builder.linking.InputObjectLinkStrategy;
import org.connectorio.addons.binding.canopen.ta.tapi.TACanString;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.i18n.EnglishLanguage;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.i18n.I18n;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.i18n.Language;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.publisher.AnalogInputRunnable;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.publisher.InputUnitsRunnable;
import org.connectorio.addons.binding.canopen.ta.tapi.dev.publisher.DigitalInputRunnable;
import org.connectorio.addons.binding.canopen.ta.tapi.func.TAFunction;
import org.connectorio.addons.binding.canopen.ta.tapi.io.AnalogGroup;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogInput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAAnalogOutput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TACanInputObject;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TACanInputOutputObject;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TADigitalInput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TADigitalOutput;
import org.connectorio.addons.binding.canopen.ta.tapi.io.TAFixedValueObject;
import org.connectorio.addons.binding.canopen.ta.tapi.val.AnalogValue;
import org.connectorio.addons.binding.canopen.ta.tapi.val.DigitalValue;
import org.connectorio.addons.binding.canopen.ta.tapi.val.Value;
import org.openhab.core.common.NamedThreadFactory;
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
  private final AtomicBoolean initialized = new AtomicBoolean();
  private ScheduledExecutorService broadcaster;

  private /*final*/ TACanString name;
  private /*final*/ TACanString function;
  private /*final*/ TACanString version;
  private /*final*/ TACanString serial;
  private /*final*/ TACanString productionDate;
  private /*final*/ TACanString bootsector;
  private /*final*/ TACanString hardwareCover;
  private /*final*/ TACanString hardwareMains;
  private CompletableFuture<Language> language;

  public TADevice(CoNode node, int clientId, boolean identifyOnly, int functions, int physicalInputLimit, int physicalOutputLimit) {
    this.node = node;
    this.clientId = clientId;
    this.maxFunctions = functions;
    this.physicalInput = physicalInputLimit;
    this.physicalOutput = physicalOutputLimit;

    subscribe(node.getNodeId(), CANOpenService.RECEIVE_PDO_3, new StatusCallback(clientId, node.getNodeId(), new Consumer<Boolean>() {
      @Override
      public void accept(Boolean loggedIn) {
        if (loggedIn) {
          name = new TACanString(node, (short) 0x2512, (short) 0x00);
          version = new TACanString(node, (short) 0x57E0, (short) 0x00);
          serial = new TACanString(node, (short) 0x57E0, (short) 0x01);
          productionDate = new TACanString(node, (short) 0x57E0, (short) 0x02);
          bootsector = new TACanString(node, (short) 0x57E0, (short) 0x03);
          hardwareCover = new TACanString(node, (short) 0x57E0, (short) 0x04);
          hardwareMains = new TACanString(node, (short) 0x57E0, (short) 0x05);
          function = new TACanString(node, (short) 0x57E0, (short) 0x07);
          language = I18n.get(node);
        }
        statusCallbacks.forEach(callback -> callback.accept(loggedIn));
      }
    }));

    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_4, new ConfigurationCallback(this, objectFactory));

    subscribe(node.getNodeId(), CANOpenService.RECEIVE_PDO_1, new AnalogOutputCallback(this, 0));
    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_2, new AnalogOutputCallback(this, 4));
    subscribe(node.getNodeId(), CANOpenService.RECEIVE_PDO_2, new AnalogOutputCallback(this, 8));
    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_3, new AnalogOutputCallback(this, 12));
    subscribe(node.getNodeId(), CANOpenService.TRANSMIT_PDO_1, new DigitalOutputCallback(this)); // digital

    subscribe(node.getNodeId() + 0x40, CANOpenService.RECEIVE_PDO_1, new AnalogOutputCallback(this, 16));
    subscribe(node.getNodeId() + 0x40, CANOpenService.TRANSMIT_PDO_2, new AnalogOutputCallback(this, 20));
    subscribe(node.getNodeId() + 0x40, CANOpenService.RECEIVE_PDO_2, new AnalogOutputCallback(this, 24));
    subscribe(node.getNodeId() + 0x40, CANOpenService.TRANSMIT_PDO_3, new AnalogOutputCallback(this, 28));


    //timer.schedule(new InputWriterTask(this), 60_000);
    broadcaster = Executors.newScheduledThreadPool(1, new NamedThreadFactory("ta-device-input-writer-" + node.getNodeId()) {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread thread = super.newThread(runnable);
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread thread, Throwable error) {
            logger.error("Thread {} ended with unhandled error {}", thread, error);
          }
        });
        thread.setContextClassLoader(getClass().getClassLoader());
        return thread;
      }
    });
    broadcaster.scheduleAtFixedRate(new AnalogInputRunnable(node, analogInput, clientId), 1, 1, TimeUnit.MINUTES);
    broadcaster.scheduleAtFixedRate(new DigitalInputRunnable(node, digitalInput, clientId), 1, 1, TimeUnit.MINUTES);
    broadcaster.scheduleAtFixedRate(new InputUnitsRunnable(node, analogInput, digitalInput, clientId), 1, 5, TimeUnit.MINUTES);
    /* test of automated I/O discovery
    broadcaster.schedule(new Runnable() {
      @Override
      public void run() {
        discoverAnalogOutput(new TAAnalogOutput(TADevice.this, 1, AnalogUnit.CELSIUS.getIndex(), (short) 0));
        discoverAnalogOutput(new TAAnalogOutput(TADevice.this, 2, AnalogUnit.CELSIUS.getIndex(), (short) 0));
        discoverAnalogOutput(new TAAnalogOutput(TADevice.this, 3, AnalogUnit.TEMPERATURE_REGULATOR.getIndex(), (short) 0x46ea));
      }
    }, 300, TimeUnit.MILLISECONDS);
    // */
  }

  public CompletableFuture<String> getName() {
    return name.toFuture();
  }
  public CompletableFuture<String> getFunction() {
    return function.toFuture();
  }
  public CompletableFuture<String> getVersion() {
    return version.toFuture();
  }
  public CompletableFuture<String> getSerial() {
    return serial.toFuture();
  }
  public CompletableFuture<String> getProductionDate() {
    return productionDate.toFuture();
  }
  public CompletableFuture<String> getBootsector() {
    return bootsector.toFuture();
  }
  public CompletableFuture<String> getHardwareCover() {
    return hardwareCover.toFuture();
  }
  public CompletableFuture<String> getHardwareMains() {
    return hardwareMains.toFuture();
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

  public CompletableFuture<Void> write(int index, Value<?> value) {
    logger.debug("Received update of input {} with new value {}", index, value);
    if (value instanceof AnalogValue) {
      analogInput.get(index).update((AnalogValue) value);
      return CompletableFuture.supplyAsync(() -> {
        new AnalogInputRunnable(node, analogInput, clientId).sendAnalog(new AnalogGroup(clientId, index));
        return null;
      });
    } else if (value instanceof DigitalValue) {
      digitalInput.get(index).update((DigitalValue) value);
      return CompletableFuture.runAsync(new DigitalInputRunnable(node, digitalInput, clientId));
    }

    logger.error("Unsupported write value {} {}.", index, value);
    CompletableFuture<Void> future = new CompletableFuture<>();
    future.completeExceptionally(new IllegalArgumentException("Unsupported object type"));
    return future;
  }

  public void sendUnits() {
    new InputUnitsRunnable(node, analogInput, digitalInput, clientId).run();
  }

  public Language getLanguage() {
    if (language.isDone()) {
      return language.getNow(new EnglishLanguage());
    }
    logger.warn("Requesting access to device language settings before it is retrieved. Assuming default to be English.");
    return new EnglishLanguage();
  }

  public void reload() {
    //scanFunctions();
    //scanFixedValues();
    scanAnalogInputs();
    scanDigitalInputs();

    reloadOutputs();
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
    broadcaster.shutdownNow();
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

  public boolean isInitialized() {
    return initialized.get();
  }

  public void setInitialized(boolean initialized) {
    this.initialized.set(initialized);
  }

  void updateAnalog(int index, short value) {
    logger.trace("Received update of analog output {} with value {} (0x{})", index, value, Integer.toHexString(value));

    if (!analogOutputs.containsKey(index)) {
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

  public void addAnalogOutput(int index, TAAnalogOutput output) {
    if (analogOutputs.containsKey(index)) {
      TAAnalogOutput base = analogOutputs.get(index);
      if (base.getUnit() != output.getUnit() && base.getUnit() == AnalogUnit.DIMENSIONLESS.getIndex()) {
        analogOutputs.put(index, output);
        logger.debug("Update of analog output {} unit from {} to {}", index, base.getUnit(), output.getIndex());
      } else {
        logger.debug("Skipping update of analog output {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      analogOutputs.put(index, output);
    }
  }

  public void addAnalogInput(int index, TAAnalogInput input) {
    if (analogInput.containsKey(index)) {
      TAAnalogInput base = analogInput.get(index);
      if (base.getUnit() != input.getUnit() && base.getUnit() == AnalogUnit.DIMENSIONLESS.getIndex()) {
        analogInput.put(index, input);
        logger.debug("Update of analog input {} unit from {} to {}", index, base.getUnit(), input.getIndex());
      } else {
        logger.debug("Skipping update of analog input {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      analogInput.put(index, input);
    }
  }

  public void addDigitalOutput(int index, TADigitalOutput output) {
    if (digitalOutputs.containsKey(index)) {
      TADigitalOutput base = digitalOutputs.get(index);
      if (base.getUnit() != output.getIndex() && base.getUnit() == DigitalUnit.CLOSE_OPEN.getIndex()) {
        digitalOutputs.put(index, output);
        logger.debug("Update of digital output {} unit from {} to {}", index, base.getUnit(), output.getIndex());
      } else {
        logger.debug("Skipping update of digital output {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      digitalOutputs.put(index, output);
    }
  }

  public void addDigitalInput(int index, TADigitalInput input) {
    if (digitalInput.containsKey(index)) {
      TADigitalInput base = digitalInput.get(index);
      if (base.getUnit() != input.getUnit() && base.getUnit() == DigitalUnit.CLOSE_OPEN.getIndex()) {
        digitalInput.put(index, input);
        logger.debug("Update of digital input {} unit from {} to {}", index, base.getUnit(), input.getIndex());
      } else {
        logger.debug("Skipping update of digital input {}, object already initialized with non-default unit {}", index, base.getUnit());
      }
    } else {
      digitalInput.put(index, input);
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

  void discoverAnalogInput(TAAnalogInput input) {
    analogInput.put(input.getIndex(), input);
    logger.info("Discovered new analog input {}", input);
    inOutCallbacks.forEach(callback -> callback.accept(input));
  }

  void discoverDigitalInput(TADigitalInput input) {
    digitalInput.put(input.getIndex(), input);
    logger.info("Discovered new digital input {}", input);
    inOutCallbacks.forEach(callback -> callback.accept(input));
  }

  protected void subscribe(int nodeId, CANOpenService service, Consumer<byte[]> consumer) {
    node.getConnection().subscribe(nodeId, service, consumer).whenComplete((sub, err) -> {
      if (err == null) {
        logger.info("Successfully subscribed {} to notifications from COB {} (node {}).", consumer, Integer.toHexString(service.getMin() + nodeId), nodeId);
      }
    });
  }

  private void reloadOutputs() {
    logger.info("Reload {} outputs.", node.getNodeId());
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
      int objectIndex = index;
      new TACanString(node, (short) 0x280F, index).toFuture().whenComplete((label, error) -> {
        if (error == null) {
          if (!label.trim().isEmpty()) {
            logger.info("Detected function {} {}", objectIndex, label);
            node.read((short) 0x2800, (short) objectIndex).whenComplete((value, fail) -> {
              logger.info("Function type {}", Hex.encodeHexString(value), fail);
            });
          }
        } else {
          logger.info("Function {} does not exist. {}", objectIndex, error.getMessage());
        }
      });
    }
  }

  private void scanFixedValues() {
    logger.debug("Scan fixed values");

    scanObjects(64, (index) -> new TAFixedValueObject<Value<?>>(this, index, 0),
      (fixed) -> logger.info("Discovered fixed value {}", fixed.getName())
    );

    /*
    for (short index = 0; index <= 10; index++) {
      int objectIndex = index;
      new TACanString(node, (short) 0x240f, (short) objectIndex).toFuture().whenComplete((label, error) -> {
        if (error == null) {
          if (!label.trim().isEmpty()) {
            logger.info("Detected fixed value {} {}", objectIndex, label);
            node.read((short) 0x2414, (short) objectIndex).whenComplete((value, fail) -> {
              logger.info("Present value {}", Hex.encodeHexString(value), fail);
            });
          }
        } else {
          logger.info("Fixed value {} does not exist. {}", objectIndex, error.getMessage());
        }
      });
    }
     */
  }

  private void scanAnalogInputs() {
    logger.debug("Scanning of CAN analog inputs");

    scanObjects(64, (index) -> new TAAnalogInput(this, index, 0),
      // force remapping of input so its index is reflected properly
      new InputValidator<>(clientId, (input) -> discoverAnalogInput(input)));
  }

  private void scanDigitalInputs() {
    logger.debug("Scanning of CAN digital inputs");

    scanObjects(64, (index) -> new TADigitalInput(this, index, 0),
      // force remapping of input so its index is reflected properly
      new InputValidator<>(clientId, (input) -> discoverDigitalInput(input)));
  }

  private <X extends TACanInputOutputObject<?>> void scanObjects(int limit, Function<Integer, X> constructor, Consumer<X> callback) {
    for (short index = 1; index <= limit; index++) {
      logger.trace("Checking if CAN object #{} is active", (int) index);
      int objectIndex = index;

      X object = constructor.apply(objectIndex);
      object.getName().whenComplete((config, error) -> {
        if (error != null) {
          logger.warn("Failed to load CAN object #{}", objectIndex, error);
          return;
        }

        callback.accept(object);
      });
    }
  }

  @Override
  public String toString() {
    return "TADevice [" + node + ", " + (name == null ? "name not retrieved" : name) + "]";
  }

  static class InputValidator<X extends TACanInputObject<?>> implements Consumer<X> {

    private final Logger logger = LoggerFactory.getLogger(InputValidator.class);
    private final int clientId;
    private final Consumer<X> delegate;

    InputValidator(int clientId, Consumer<X> delegate) {
      this.clientId = clientId;
      this.delegate = delegate;
    }

    @Override
    public void accept(X input) {
      input.getConfiguration().whenComplete((config, error) -> {
        String inputType = input instanceof TAAnalogInput ? "analog" : "digital";
        if (error != null) {
          logger.warn("Failed to load CAN Input #{} ({})", input.getIndex(), inputType, error);
          return;
        }
        if (config.getNode() != clientId) {
          logger.debug("Ignore CAN Input #{} ({}) since it is indented for node {}.", input.getIndex(), inputType, config.getNode());
          return;
        }
        if (config.getIndex() == -1) {
          logger.debug("Ignore CAN Input #{} ({}) since its index is not defined.", input.getIndex(), inputType);
          return;
        }

        logger.trace("CAN Input #{} ({}) passed validation.", input.getIndex(), inputType);
        delegate.accept(input);
      });
    }
  }

}
