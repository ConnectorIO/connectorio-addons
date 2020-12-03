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
import java.util.function.Consumer;
import javax.measure.Quantity;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.binding.plc4x.canopen.config.CANopenNodeConfig;
import org.connectorio.binding.plc4x.canopen.handler.CANopenBridgeHandler;
import org.connectorio.binding.plc4x.canopen.ta.internal.handler.protocol.TAOperations;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAADigitalOutput;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAAnalogOutput;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAObject;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAOutput;
import org.connectorio.binding.plc4x.canopen.ta.internal.type.TAValue;
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
import tec.uom.se.AbstractUnit;

public class TAUVR16x2ThingHandler extends PollingPlc4xThingHandler<PlcConnection, CANopenBridgeHandler<?>, CANopenNodeConfig>
  implements Plc4xThingHandler<PlcConnection, CANopenBridgeHandler<?>, CANopenNodeConfig>, Consumer<TAObject> {

  private final Logger logger = LoggerFactory.getLogger(TAUVR16x2ThingHandler.class);
  private int nodeId;
  private int clientId;
  private TAOperations operations;

  public TAUVR16x2ThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    CANopenNodeConfig config = getConfigAs(CANopenNodeConfig.class);
    nodeId = config.nodeId;
    clientId = getBridgeHandler().map(CANopenBridgeHandler::getNodeId).orElse(-1);

    updateStatus(ThingStatus.OFFLINE);

    getBridgeConnection().ifPresent(connection -> {
      logger.debug("Retrieving UVR {} configuration", nodeId);
      operations = new TAOperations(connection);

      ValueListener valueListener = new ThingChannelValueListener(getCallback(), getThing(), this::createState);

      operations.subscribeStatus(connected -> {
        if (connected) {
          operations.reload(nodeId);
        }
        updateStatus(connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
      }, nodeId, clientId);
      operations.subscribeInputOutputState(valueListener, nodeId);
      operations.subscribeInputOutputConfig(this, nodeId);
      operations.login(nodeId, clientId).whenComplete((response, error) -> {
        if (error != null) {
          logger.error("Could not complete initialization, device login failed", error);
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
        }
      });
      ;
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

    TypeEntry channelType = ChannelTypeHelper.channelType(output);
    String label = output.getLabel().orElse(fallbackLabel + output.getIndex());

    ThingBuilder thingBuilder = editThing();
    if (getThing().getChannel(channelUID) != null) {
      thingBuilder.withoutChannel(channelUID);
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("index", output.getIndex());
    properties.put("unit", output.getUnit());
    Configuration configuration = new Configuration(properties);

    ChannelBuilder channelBuilder = ChannelBuilder.create(new Channel(channelUID, channelType.getItemType()))
      .withKind(ChannelKind.STATE)
      .withLabel(label)
      .withType(channelType.getChannelType())
      .withConfiguration(configuration);
    thingBuilder.withChannel(channelBuilder.build());

    updateThing(thingBuilder.build());

    Optional.ofNullable(getCallback()).ifPresent(callback -> output.getState().ifPresent(value -> callback.stateUpdated(channelUID, createState(value))));
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
      return new QuantityType(quantity.getValue(), quantity.getUnit());
    }

    if (value instanceof Number) {
      return QuantityType.valueOf(((Number) value).doubleValue(), AbstractUnit.ONE);
    }

    // unknown
    return UnDefType.UNDEF;
  }

}
