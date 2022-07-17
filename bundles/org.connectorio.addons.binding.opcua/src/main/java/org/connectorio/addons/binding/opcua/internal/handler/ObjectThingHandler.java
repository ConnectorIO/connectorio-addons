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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.opcua.internal.config.NodeConfig;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaObjectNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectThingHandler extends GenericThingHandlerBase<ClientBridgeHandler, NodeConfig> implements ThingHandler {

  private Logger logger = LoggerFactory.getLogger(ObjectThingHandler.class);
  private NodeId nodeId;
  private UaObjectNode node;

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

    switch (config.identifierType) {
      case i:
        this.nodeId = new NodeId(config.ns, UInteger.valueOf(config.identifier));
        break;
      case s:
        this.nodeId = new NodeId(config.ns, config.identifier);
        break;
      case g:
        this.nodeId = new NodeId(config.ns, UUID.fromString(config.identifier));
        break;
      case b:
        this.nodeId = new NodeId(config.ns, ByteString.of(config.identifier.getBytes()));
        break;
    }

    getBridgeHandler().ifPresent(bridge -> {
      bridge.getClient().whenComplete((client, error) -> {
        if (error != null) {
          updateStatus(ThingStatus.ONLINE, ThingStatusDetail.BRIDGE_OFFLINE);
          return;
        }

        client.getAddressSpace().getObjectNodeAsync(this.nodeId).whenComplete((node, readErr) -> {
          if (readErr != null) {
            logger.warn("Could not retrieve object node from server {}", nodeId, readErr);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Remote server returned error " + readErr.getMessage());
            return;
          }

          this.node = node;
          updateStatus(ThingStatus.ONLINE);
          logger.debug("Finished initialization of thing handler for node {}, retrieved UA object {}.", nodeId, node);
          fetchChannels(client, node);
        });
      });
    });
  }

  private void fetchChannels(OpcUaClient client, UaObjectNode node) {
    List<Channel> channels = getThing().getChannels();
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
    client.browse(browse).whenComplete((result, error) -> {
      if (error != null) {
        logger.warn("Could not retrieve object variables from server {}", nodeId, error);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Remote server returned error while retrieving variables" + error.getMessage());
        return;
      }

      ThingBuilder thing = editThing();
      List<Channel> channelDef = new ArrayList<>();
      for (ReferenceDescription reference : result.getReferences()) {
        logger.info("Potential channel found " + reference.getDisplayName() + " " + reference.getTypeDefinition() + " " + reference.getTypeId());
      }
      updateThing(thing.withChannels(channelDef).build());
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }
}
