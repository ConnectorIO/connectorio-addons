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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import javax.measure.Quantity;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.plc4x.canopen.handler.CoBridgeHandler;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ComplexUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.ControllerConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.DigitalUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.protocol.TAOperations;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.provider.ChannelTypeEntry;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAObject;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAOutput;
import org.connectorio.addons.binding.plc4x.canopen.config.CoNodeConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.AnalogUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAADigitalOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAAnalogOutput;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAValue;
import org.connectorio.addons.binding.plc4x.handler.Plc4xThingHandler;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xThingHandler;
import org.connectorio.plc4x.decorator.CompositeDecorator;
import org.connectorio.plc4x.decorator.DecoratorConnection;
import org.connectorio.plc4x.decorator.phase.Phase;
import org.connectorio.plc4x.decorator.phase.PhaseDecorator;
import org.connectorio.plc4x.decorator.retry.RetryDecorator;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.AbstractUnit;

public class TAUVR16x2ThingHandler extends PollingPlc4xThingHandler<PlcConnection, CoBridgeHandler<?>, CoNodeConfig>
  implements Plc4xThingHandler<PlcConnection, CoBridgeHandler<?>, CoNodeConfig>, Consumer<TAObject> {

  private final Logger logger = LoggerFactory.getLogger(TAUVR16x2ThingHandler.class);
  private int nodeId;
  private int clientId;
  private TAOperations operations;
  private final Semaphore semaphore;
  private final Timer logoutTimer;

  public TAUVR16x2ThingHandler(Thing thing, Semaphore semaphore) {
    super(thing);
    this.logoutTimer = new Timer("canopen-ta-logout-" + thing.getLabel(), true);
    this.semaphore = semaphore;
  }

  @Override
  public void initialize() {
    ControllerConfig config = getConfigAs(ControllerConfig.class);
    nodeId = config.nodeId;
    clientId = getBridgeHandler().map(CoBridgeHandler::getNodeId).orElse(-1);

    updateStatus(ThingStatus.OFFLINE);

    getBridgeConnection().ifPresent(connection -> {
      // block initialisation of other handlers until we complete reading all SDOs
      semaphore.acquireUninterruptibly();

      logger.debug("Retrieving UVR {} configuration", nodeId);
      operations = new TAOperations(connection);

      ValueListener valueListener = new ThingChannelValueListener(getCallback(), getThing(), this::createState);

      Phase phase = new Phase("Initialize " + thing.getUID() + " " + thing.getLabel());
      phase.onCompletion(new Runnable() {
        @Override
        public void run() {
          operations.logout(nodeId, clientId);
        }
      });

      operations.subscribeStatus(connected -> {
        if (connected) {
          operations.reload(nodeId);
          // lets release SDO lock within 90 seconds, should be sufficient to complete all SDO detection in most of cases
          updateStatus(ThingStatus.ONLINE);
          return;
        }
        if (config.ignoreLoginFailure) {
          operations.reload(nodeId);
          updateStatus(ThingStatus.ONLINE);
        } else {
          updateStatus(ThingStatus.OFFLINE);
        }
      }, nodeId, clientId);
      operations.subscribeInputOutputState(valueListener, nodeId);
      operations.subscribeInputOutputConfig(this, nodeId);
      operations.login(nodeId, clientId).whenComplete((response, error) -> {
        if (error != null) {
          logger.error("Could not complete initialization, device login failed", error);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
        }
      });
    });
  }

  @Override
  public void dispose() {
    if (operations != null) {
      operations.logout(nodeId, clientId);
      operations.close();
    }
    super.dispose();
  }

  @Override
  protected void updateStatus(ThingStatus status) {
    super.updateStatus(status);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

  @Override
  public void accept(TAObject object) {
    logger.debug("Discovered new input/output {} for node {}", object, nodeId);
    if (object instanceof TAAnalogOutput) {
      updateThingChannel((TAOutput) object, "analog#","Analog #");
    } else if (object instanceof TAADigitalOutput) {
      updateThingChannel((TAOutput) object, "digital#", "Digital #");
    } else {
      logger.info("Found unsupported object {}", object);
    }
  }

  private void updateThingChannel(TAOutput output, String channelId, String fallbackLabel) {
    ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId + output.getIndex());

    ChannelTypeEntry channelType = ChannelTypeHelper.channelType(output);
    logger.debug("Selected channel {} type {} for {} on node {}", channelUID, channelType, output, nodeId);
    String label = output.getLabel().orElse(fallbackLabel + output.getIndex());

    ThingBuilder thingBuilder = editThing();
    if (getThing().getChannel(channelUID) != null) {
      thingBuilder.withoutChannel(channelUID);
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("index", output.getIndex());
    if (output instanceof TAAnalogOutput) {
      TAUnit unit = AnalogUnit.valueOf(output.getUnit());
      if (unit != null) {
        properties.put("unit", unit.name());
      } else {
        ComplexUnit complexUnit = ComplexUnit.valueOf(output.getUnit());
        if (complexUnit != null) {
          properties.put("unit", complexUnit.name());
        } else {
          logger.warn("Received output with unsupported analog unit {} ({}), falling back to dimensionless", output.getUnit(), Integer.toHexString(output.getUnit()));
          properties.put("unit", AnalogUnit.DIMENSIONLESS.name());
        }
      }
    } else if (output instanceof TAADigitalOutput) {
      DigitalUnit unit = DigitalUnit.valueOf(output.getUnit());
      if (unit != null) {
        properties.put("unit", unit.name());
      } else {
        logger.warn("Received output with unsupported digital unit {} ({}), falling back to basic ON/OFF unit", output.getUnit(), Integer.toHexString(output.getUnit()));
        properties.put("unit", DigitalUnit.ON_OFF.name());
      }
    }
    Configuration configuration = new Configuration(properties);

    ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID)
      .withKind(ChannelKind.STATE)
      .withAcceptedItemType(channelType.getItemType())
      .withLabel(label)
      .withType(channelType.getChannelType())
      .withConfiguration(configuration);
    thingBuilder.withChannel(channelBuilder.build());

    try {
      updateThing(thingBuilder.build());

      Optional.ofNullable(getCallback())
        .ifPresent(callback -> output.getState().ifPresent(value -> callback.stateUpdated(channelUID, createState(value))));
    } catch (Exception e) {
      logger.error("Failed to configure channel {} for node {}", channelUID, nodeId, e);
    }
  }

  private State createState(TAValue taValue) {
    Object value = taValue.getValue();
    if (value instanceof State) {
      // digital
      return (State) value;
    }

    // analog
    if (value instanceof Quantity) {
      Quantity<?> quantity = (Quantity<?>) value;
      return new QuantityType<>(quantity.getValue(), quantity.getUnit());
    }

    if (value instanceof Number) {
      return QuantityType.valueOf(((Number) value).doubleValue(), AbstractUnit.ONE);
    }

    if (value instanceof List<?>) {
      List<?> list = (List<?>) value;
      if (list.size() > 0 && list.get(0) instanceof Quantity) {
        Quantity<?> quantity = (Quantity<?>) list.get(0);
        return new QuantityType<>(quantity.getValue(), quantity.getUnit());
      }
    }

    // unknown
    return UnDefType.UNDEF;
  }

}
