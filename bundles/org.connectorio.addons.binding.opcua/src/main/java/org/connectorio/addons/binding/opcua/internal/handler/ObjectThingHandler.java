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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import org.connectorio.addons.binding.config.PollingConfiguration;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.opcua.internal.config.ClientConfig;
import org.connectorio.addons.binding.opcua.internal.config.NodeConfig;
import org.eclipse.milo.opcua.sdk.client.AddressSpace.BrowseOptions;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem.ValueConsumer;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription.ItemCreationCallback;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription.NotificationListener;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaSubscriptionManager;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.openhab.core.config.core.Configuration;
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
    if (config.identifier == null || config.identifier.trim().length() == 0) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid identifier");
      return;
    }

    this.nodeId = config.createNodeId();

    getBridgeHandler().ifPresent(bridge -> {
      bridge.getClient().whenComplete((client, error) -> {
        if (error != null) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
          return;
        }

        if (!thing.getChannels().isEmpty()) {
          this.channelMap = new HashMap<>();
          this.nodeMap = new HashMap<>();
          for (Channel channel : thing.getChannels()) {
            Configuration configuration = channel.getConfiguration();
            NodeId nodeId = configuration.as(NodeConfig.class).createNodeId();
            this.channelMap.put(channel.getUID(), nodeId);
            this.nodeMap.put(nodeId, channel.getUID());
          }

          Long refreshInterval = bridge.getBridgeConfig().map(cfg -> cfg.refreshInterval).orElse(1000L);
          subscriptionManager = client.getSubscriptionManager();
          subscriptionManager.createSubscription(refreshInterval).whenComplete((subscription, failure) -> {
            if (failure != null) {
              logger.error("Failed to initialize subscription", failure);
              return;
            }
            this.subscription = subscription;
            initializeSubscription(subscription);
          });
          updateStatus(ThingStatus.ONLINE);
          return;
        }

        // initialize default channel list based on browse answer
        client.getAddressSpace().getNodeAsync(this.nodeId).whenComplete((node, readErr) -> {
          if (readErr != null) {
            logger.warn("Could not retrieve object node from server {}", nodeId, readErr);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Remote server returned error " + readErr.getMessage());
            return;
          }
          updateStatus(ThingStatus.ONLINE);
          logger.debug("Finished initialization of thing handler for node {}, retrieved UA object {}.", nodeId, node);
          fetchChannels(client, node).whenComplete((discoveredChannels, throwable) -> {
            if (throwable != null) {
              logger.warn("Could not retrieve object variables from server {}", nodeId, throwable);
              updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                  "Remote server returned error while retrieving variables" + throwable.getMessage());
              return;
            }
            if (discoveredChannels != null && !discoveredChannels.isEmpty()) {
              logger.debug("Updating OPC UA thing {} definition with {} discovered channels.", nodeId, discoveredChannels.size());
              Thing updatedThing = editThing().withChannels(discoveredChannels).build();
              updateThing(updatedThing);
            }
          }).whenComplete((discoveredThing, throwable) -> {
            if (throwable != null) {
              logger.warn("Could not update OPC UA thing {} channels.", nodeId, throwable);
            }
          });
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
    List<MonitoredItemCreateRequest> monitoredItems = new ArrayList<>();
    for (NodeId nodeId : this.nodeMap.keySet()) {
      MonitoredItemCreateRequest monitoredItem = createMonitoredItem(nodeId);
      monitoredItems.add(monitoredItem);
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
        return;
      }
      logger.info("Creation of monitored items succeeded");
    });
    subscription.addNotificationListener(new NotificationListener() {
      @Override
      public void onDataChangeNotification(UaSubscription subscription, List<UaMonitoredItem> monitoredItems, List<DataValue> dataValues, DateTime publishTime) {
        for (int index = 0; index < monitoredItems.size(); index++) {
          UaMonitoredItem item = monitoredItems.get(index);
          DataValue dataValue = dataValues.get(index);
          NodeId nodeId = item.getReadValueId().getNodeId();
          logger.trace("Thing {} received data change notification from {} for channel {}, {}", thing.getUID(), publishTime, nodeId, dataValue.getValue());
          ObjectThingHandler.this.onValueArrived(item, dataValue);
        }
      }
    });
  }

  private MonitoredItemCreateRequest createMonitoredItem(NodeId objectId) {
    MonitoringParameters parameters = new MonitoringParameters(
      uint(SUBSCRIPTION_COUNTER.incrementAndGet()),
      1000.0,
      null,
      uint(10),
      true
    );

    ReadValueId readValueId = new ReadValueId(
      objectId, AttributeId.Value.uid(),
      null,
      QualifiedName.NULL_VALUE
    );
    return new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
  }

  private CompletableFuture<List<Channel>> fetchChannels(OpcUaClient client, UaNode node) {
    BrowseOptions options = new BrowseOptions(
      BrowseDirection.Forward,
      Identifiers.HierarchicalReferences,
      true,
      uint(NodeClass.Variable.getValue()),
      uint(0)
    );
    return client.getAddressSpace().browseNodesAsync(node, options).thenApply(result -> {
      Map<ChannelUID, Channel> channelDefinitions = new LinkedHashMap<>();
      for (UaNode variable : result) {
        NodeId variableNodeId = variable.getNodeId();
        logger.info("Potential channel found {} {}", variableNodeId, variable.getDescription());
        Map<String, Object> config = NodeConfig.createNodeConfig(variableNodeId);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId(variableNodeId));
        ChannelBuilder channelBuilder = determineChannelType(variable, ChannelBuilder.create(channelUID)
          .withLabel(Optional.ofNullable(variable.getDisplayName()).map(LocalizedText::getText).orElse(nodeId.toString()))
          .withDescription(Optional.ofNullable(variable.getDescription()).map(LocalizedText::getText).orElse(""))
          .withConfiguration(new Configuration(config))
        );
        if (channelBuilder != null) {
          Channel channel = channelBuilder.build();
          appendChannel(channelDefinitions, channelUID, channel, variableNodeId);
        }
      }
      return new ArrayList<>(channelDefinitions.values());
    });
  }

  private void appendChannel(Map<ChannelUID, Channel> channelDefinitions, ChannelUID channelUID,
      Channel channel, NodeId variableNodeId) {
    if (!channelDefinitions.containsKey(channelUID)) {
      logger.debug("Discovered channel {} from node {}", channelUID, variableNodeId);
      channelDefinitions.put(channelUID, channel);
    } else {
      // generate unique id by appending sequence number to the end of ID
      ChannelUID uniqueId =  channelUID;
      int seq = 0;
      do {
        uniqueId = new ChannelUID(uniqueId.getThingUID(), uniqueId.getId() + "_" + seq++);
      } while (channelDefinitions.containsKey(uniqueId));
      logger.debug("Detected duplicate channel ID {} from node {}, using channel {} instead", channelUID, variableNodeId, uniqueId);

      Channel uniqueChannel = ChannelBuilder.create(uniqueId)
        .withType(channel.getChannelTypeUID())
        .withLabel(channel.getLabel())
        .withDescription(channel.getDescription())
        .withConfiguration(channel.getConfiguration())
        .withKind(channel.getKind())
        .build();
      appendChannel(channelDefinitions, uniqueId, uniqueChannel, variableNodeId);
    }
  }

  private String channelId(NodeId nodeId) {
    String channelId = nodeId.getNamespaceIndex() + "_";
    String id = (nodeId.getIdentifier().toString()).replaceAll("\\W+", "_");
    return channelId + id;
  }

  private ChannelBuilder determineChannelType(UaNode node, ChannelBuilder channelBuilder) {
    if (!(node instanceof UaVariableNode)) {
      return null;
    }
    UaVariableNode variable = (UaVariableNode) node;
    if (variable.getDataType().equals(Identifiers.SByte)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "signed-byte"));
    } else if (variable.getDataType().equals(Identifiers.Byte)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "byte"));
    } else if (variable.getDataType().equals(Identifiers.Int16)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "int16"));
    } else if (variable.getDataType().equals(Identifiers.UInt16)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "uint16"));
    } else if (variable.getDataType().equals(Identifiers.Int32)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "int32"));
    } else if (variable.getDataType().equals(Identifiers.UInt32)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "uint32"));
    } else if (variable.getDataType().equals(Identifiers.Int64)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "int64"));
    } else if (variable.getDataType().equals(Identifiers.UInt64)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "uint64"));
    } else if (variable.getDataType().equals(Identifiers.Float)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "float"));
    } else if (variable.getDataType().equals(Identifiers.Double)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "double"));
    } else if (variable.getDataType().equals(Identifiers.String)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "string"));
    } else if (variable.getDataType().equals(Identifiers.DateTime)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "datetime"));
    } else if (variable.getDataType().equals(Identifiers.Guid)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "guid"));
    } else if (variable.getDataType().equals(Identifiers.ByteString)) {
      channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "byte-string"));
    } else {
      Variant dataValue = variable.getValue().getValue();
      Object value = dataValue.getValue();
      if (value == null) {
        logger.debug("Unable to map channel type for node {}, data type: {}, value is null", variable.getNodeId(),
            variable.getDataType());
        return null;
      } else if (value instanceof Number) {
        if (value instanceof Short) {
          channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "int16"));
        } else if (value instanceof Integer) {
          channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "int32"));
        } else if (value instanceof Long) {
          channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "int64"));
        } else {
          channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "double"));
        }
      } else if (value instanceof String || value instanceof LocalizedText) {
        channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "string"));
      } else if (value instanceof DateTime) {
        channelBuilder.withType(new ChannelTypeUID("co7io-opcua", "datetime"));
      } else {
        logger.debug("Unsupported node {}, data type: {}, value {} ({})", variable.getNodeId(), variable.getDataType(),
            value, value.getClass());
        return null;
      }
    }
    return channelBuilder;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    CompletionStage<OpcUaClient> clientCompletionStage = getBridgeHandler().map(ClientBridgeHandler::getClient)
      .orElse(null);

    if (clientCompletionStage == null) {
      logger.info("OPC UA Node handler is not ready yet, ignoring write attempt {} to channel {}", command, channelUID);
      return;
    }

    if (channelMap != null && channelMap.containsKey(channelUID)) {
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

  @Override
  public void onValueArrived(UaMonitoredItem item, DataValue value) {
    if (getCallback() == null) {
      logger.warn("OPC UA node handler received object property update, but is not ready to receive state update. Discarding update of {} with value {}", item, value);
      return;
    }

    NodeId nodeId = item.getReadValueId().getNodeId();
    if (nodeMap != null && nodeMap.containsKey(nodeId)) {
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
    } else if (val instanceof String ||  val instanceof LocalizedText) {
      return new StringType(val instanceof LocalizedText ? ((LocalizedText) val).getText() : val.toString());
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
