/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.opcua.internal.handler;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.opcua.internal.config.NodeConfig;
import org.connectorio.addons.binding.opcua.internal.config.NodeConfig.IdentifierType;
import org.eclipse.milo.opcua.sdk.client.AddressSpace.BrowseOptions;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem.ValueConsumer;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription.ItemCreationCallback;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaSubscriptionManager;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.internal.ConfigMapper;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectThingHandler extends GenericThingHandlerBase<ClientBridgeHandler, NodeConfig> implements ThingHandler, ValueConsumer {

  private static final AtomicInteger SUBSCRIPTION_COUNTER = new AtomicInteger();

  private final Logger logger = LoggerFactory.getLogger(ObjectThingHandler.class);
  private Map<NodeId, ChannelUID> nodeMap;
  private Map<ChannelUID, NodeId> channelMap;
  private NodeId nodeId;
  private OpcUaSubscriptionManager subscriptionManager;
  private UaSubscription subscription;

  public ObjectThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    NodeConfig config = getThingConfig().get();

    if (config.ns < 0) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid namespace index");
      return;
    }
    if (config.identifierType == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid identifier type");
      return;
    }
    if (config.identifier == null || config.identifier.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid identifier");
      return;
    }

    this.nodeId = createNodeId(config);

    getBridgeHandler().ifPresent(bridge -> {
      bridge.getClient().whenComplete((client, error) -> {
        if (error != null) {
          updateStatus(ThingStatus.ONLINE, ThingStatusDetail.BRIDGE_OFFLINE);
          return;
        }

        if (!thing.getChannels().isEmpty()) {
          subscriptionManager = client.getSubscriptionManager();
          subscriptionManager.createSubscription(1000).whenComplete((subscription, failure) -> {
            if (failure != null) {
              logger.error("Failed to initialize subscription", failure);
              return;
            }
            this.subscription = subscription;
            initializeSubscription(subscription);
          });
          return;
        }

        // initialize default channel list based on browse answer
        client.getAddressSpace().getObjectNodeAsync(this.nodeId).whenComplete((node, readErr) -> {
          if (readErr != null) {
            logger.warn("Could not retrieve object node from server {}", nodeId, readErr);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Remote server returned error " + readErr.getMessage());
            return;
          }
          updateStatus(ThingStatus.ONLINE);
          logger.debug("Finished initialization of thing handler for node {}, retrieved UA object {}.", nodeId, node);
          fetchChannels(client, node);
        });
      });
    });
  }

  @Override
  public void dispose() {
    if (subscriptionManager != null && subscription != null) {
      subscriptionManager.deleteSubscription(subscription.getSubscriptionId());
    }

    this.subscription = null;
    this.subscriptionManager = null;
  }

  private void initializeSubscription(UaSubscription subscription) {
    this.channelMap = new HashMap<>();
    this.nodeMap = new HashMap<>();
    List<MonitoredItemCreateRequest> monitoredItems = new ArrayList<>();
    for (Channel channel : thing.getChannels()) {
      Configuration configuration = channel.getConfiguration();
      NodeConfig as = configuration.as(NodeConfig.class);
      MonitoredItemCreateRequest monitoredItem = createMonitoredItem(as);
      monitoredItems.add(monitoredItem);

      this.channelMap.put(channel.getUID(), nodeId);
      this.nodeMap.put(nodeId, channel.getUID());
    }

    ItemCreationCallback cbk = new ItemCreationCallback() {
      @Override
      public void onItemCreated(UaMonitoredItem item, int index) {
        logger.debug("Registering value callback for node {}", item.getReadValueId());
        item.setValueConsumer(ObjectThingHandler.this);
      }
    };
    subscription.createMonitoredItems(TimestampsToReturn.Both, monitoredItems, cbk).whenComplete((monitors, errors) -> {
      if (errors != null) {
        logger.error("Failed to construct subscription for {} elements", monitoredItems.size(), errors);
      }
      logger.info("Creation of monitored items succeeded");
    });
  }

  private MonitoredItemCreateRequest createMonitoredItem(NodeConfig config) {
    MonitoringParameters parameters = new MonitoringParameters(
      uint(SUBSCRIPTION_COUNTER.incrementAndGet()),
      1000.0,
      null,
      uint(10),
      true
    );

    NodeId objectId = createNodeId(config);
    ReadValueId readValueId = new ReadValueId(
      objectId, AttributeId.Value.uid(),
      null,
      QualifiedName.NULL_VALUE
    );
    return new MonitoredItemCreateRequest(readValueId, MonitoringMode.Sampling, parameters);
  }

  private void fetchChannels(OpcUaClient client, UaObjectNode node) {
    List<Channel> channels = getThing().getChannels();
    // thing has at least one channel defined, do not generate channels
    if (!channels.isEmpty()) {
      return;
    }

    BrowseDescription browse = new BrowseDescription(
      nodeId,
      BrowseDirection.Forward,
      Identifiers.References,
      true,
      uint(NodeClass.Variable.getValue()),
      uint(BrowseResultMask.All.getValue())
    );

    BrowseOptions options = new BrowseOptions(
      BrowseDirection.Forward,
      Identifiers.HierarchicalReferences,
      true,
      uint(NodeClass.Variable.getValue()),
      uint(0)
    );
    client.getAddressSpace().browseNodesAsync(node, options).whenComplete((result, error) -> {
      if (error != null) {
        logger.warn("Could not retrieve object variables from server {}", nodeId, error);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Remote server returned error while retrieving variables" + error.getMessage());
        return;
      }

      ThingBuilder thing = editThing();
      List<Channel> channelDefinitions = new ArrayList<>();
      for (UaNode variable : result) {
        NodeId variableNodeId = variable.getNodeId();
        logger.info("Potential channel found " + variableNodeId + " " + variable.getDescription());
        Map<String, Object> config = createNodeConfig(variableNodeId);

        // todo finish channel construction
        String channelId = variableNodeId.getNamespaceIndex() + "_" + variableNodeId.getIdentifier();
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        ChannelBuilder channelBuilder = ChannelBuilder.create(channelUID)
          .withLabel(Optional.ofNullable(variable.getDisplayName().getText()).orElse(nodeId.toString()))
          .withDescription(Optional.ofNullable(variable.getDescription().getText()).orElse(""))
          .withConfiguration(new Configuration(config))
          // temporary
          .withType(new ChannelTypeUID("co7io-opcua", "double"))
          .withAcceptedItemType(CoreItemFactory.NUMBER)
//          .withType();
          ;
        channelDefinitions.add(channelBuilder.build());
      }

      updateThing(thing.withChannels(channelDefinitions).build());
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    CompletionStage<OpcUaClient> clientCompletionStage = getBridgeHandler().map(ClientBridgeHandler::getClient)
      .orElse(null);

    if (clientCompletionStage == null) {
      logger.info("OPC UA Node handler is not ready yet, ignoring write attempt {} to channel {}", command, channelUID);
      return;
    }

    if (channelMap.containsKey(channelUID)) {
      // we have node for this channel
      NodeId nodeId = channelMap.get(channelUID);
      DataValue dataValue = mapCommand(command);

      if (dataValue == null) {
        logger.warn("Write of value {} is not supported. Node {} will not be updated", command, nodeId);
        return;
      }
      clientCompletionStage.thenAccept(client -> {
        client.writeValue(nodeId, dataValue);
      });
      return;
    }

    logger.info("Ignoring command {}. Channel {} is not mapped to any OPC UA node. ", command, channelUID);
  }

  private NodeId createNodeId(NodeConfig config) {
    switch (config.identifierType) {
      case i:
        return new NodeId(config.ns, UInteger.valueOf(config.identifier));
      case s:
        return new NodeId(config.ns, config.identifier);
      case g:
        return new NodeId(config.ns, UUID.fromString(config.identifier));
      case b:
        return new NodeId(config.ns, ByteString.of(config.identifier.getBytes()));
    }
    return null;
  }

  private Map<String, Object> createNodeConfig(NodeId nodeId) {
    Map<String, Object> config = new HashMap<>();
    config.put("ns", nodeId.getNamespaceIndex().intValue());
    config.put("identifier", nodeId.getIdentifier());
    switch (nodeId.getType()) {
      case Numeric:
        config.put("identifierType", IdentifierType.i.name());
        break;
      case String:
        config.put("identifierType", IdentifierType.s.name());
        break;
      case Guid:
        config.put("identifierType", IdentifierType.g.name());
        break;
      case Opaque:
        config.put("identifierType", IdentifierType.b.name());
        break;
    }

    return config;
  }

  @Override
  public void onValueArrived(UaMonitoredItem item, DataValue value) {
    if (getCallback() == null) {
      logger.warn("OPC UA node handler received object property update, but is not ready to receive state update. Discarding update of {} with value {}", item, value);
      return;
    }

    NodeId nodeId = item.getReadValueId().getNodeId();
    if (nodeMap.containsKey(nodeId)) {
      ChannelUID channel = nodeMap.get(nodeId);
      State mappedValue = mapValue(value);
      if (mappedValue != null) {
        getCallback().stateUpdated(channel, mappedValue);
      }
    }
  }

  private State mapValue(DataValue value) {
    Object val = value.getValue().getValue();
    if (val instanceof Boolean) {
      return Boolean.TRUE.equals(val) ? OnOffType.ON : OnOffType.OFF;
    } else if (val instanceof Number) {
      return new DecimalType(((Number) val).doubleValue());
    } else if (val instanceof String) {
      return new StringType(val.toString());
    } else if (val instanceof DateTime) {
      ZonedDateTime dateTime = ((DateTime) val).getJavaInstant().atZone(ZoneId.systemDefault());
      return new DateTimeType(dateTime);
    }

    logger.warn("Unsupported value {} of type {}", val, val != null ? val.getClass().getName() : "<null>");
    return null;
  }

  private DataValue mapCommand(Command command) {
    if (command instanceof OnOffType) {
      Variant variant = new Variant(command == OnOffType.ON);
      return new DataValue(variant);
    }
    if (command instanceof OpenClosedType) {
      Variant variant = new Variant(command == OpenClosedType.OPEN);
      return new DataValue(variant);
    }
    if (command instanceof StringType) {
      Variant variant = new Variant(command.toString());
      return new DataValue(variant);
    }
    return null;
  }

}
