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

package org.connectorio.addons.binding.opcua.internal.discovery;

import static org.connectorio.addons.binding.opcua.OpcUaBindingConstants.OBJECT_THING_TYPE;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.connectorio.addons.binding.opcua.internal.handler.ClientBridgeHandler;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.IdType;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {

  private final Logger logger = LoggerFactory.getLogger(NodeDiscoveryService.class);

  private ClientBridgeHandler handler;

  public NodeDiscoveryService() throws IllegalArgumentException {
    super(null, 300);
  }

  @Override
  protected void startBackgroundDiscovery() {
    startScan();
  }

  @Override
  protected void startScan() {
    handler.getClient().thenAccept(opcUaClient -> {
      discover(opcUaClient, Identifiers.ObjectsFolder);
    });
  }

  private void discover(OpcUaClient client, NodeId node) {
    BrowseDescription request = new BrowseDescription(
      node,
      BrowseDirection.Forward,
      Identifiers.References,
      true,
      uint(NodeClass.Object.getValue() /* | NodeClass.Variable.getValue() | NodeClass.View.getValue()*/),
      uint(BrowseResultMask.All.getValue())
    );

    client.browse(request).whenComplete((response, error) -> {
      if (error != null) {
        logger.error("Could not complete object discovery", error);
        return;
      }
      List<ReferenceDescription> references = toList(response.getReferences());

      for (ReferenceDescription rd : references) {
        String browseName = rd.getBrowseName().getName();
        String displayName = rd.getDisplayName().getText();
        logger.debug("Discovery found OPC UA node {} (browse name), {} (display name)", browseName, displayName);

        // recursively browse to children
        Optional<NodeId> discoveredNode = rd.getNodeId().toNodeId(client.getNamespaceTable());
        discoveredNode.ifPresent(nodeId -> discover(client, nodeId));
        discoveredNode.ifPresent(nodeId -> {
          client.getAddressSpace().getNodeAsync(nodeId).whenComplete((nodeObj, readErr) -> {
            if (readErr != null) {
              logger.warn("Failure while retrieving OPC UA node {} info", nodeId, readErr);
              return;
            }
            DiscoveryResult result = toDiscoveryResult(nodeObj);
            if (result != null) {
              thingDiscovered(result);
            }
          });
        });
      }
    });
  }

  private DiscoveryResult toDiscoveryResult(UaNode object) {
    DiscoveryResultBuilder builder = null;
    String id = "ns_" + object.getNodeId().getNamespaceIndex() + "_" + object.getNodeId().getIdentifier();

    ThingUID bridgeUID = handler.getThing().getUID();
    if (object.getNodeClass() == NodeClass.Object) {
      builder = DiscoveryResultBuilder.create(new ThingUID(OBJECT_THING_TYPE, bridgeUID, id));
    } else {
      logger.info("Unsupported node class " + object.getNodeClass());
      return null;
    }

    return builder.withLabel(object.getDisplayName().getText())
      .withProperty("description", object.getDescription().getText())
      .withBridge(bridgeUID)
      .withProperty("ns", object.getNodeId().getNamespaceIndex().intValue())
      .withProperty("identifierType", mapIdType(object.getNodeId().getType()))
      .withProperty("identifier", "" + object.getNodeId().getIdentifier())
      .build();
  }

  private String mapIdType(IdType type) {
    switch (type) {
      case Numeric:
        return "i";
      case String:
        return "s";
      case Guid:
        return "g";
      case Opaque:
        return "b";
    }
    return "";
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof ClientBridgeHandler) {
      this.handler = (ClientBridgeHandler) handler;
    }
  }

  @Override
  public ThingHandler getThingHandler() {
    return this.handler;
  }

  @Override
  public void activate() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
    super.activate(properties);
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

}
