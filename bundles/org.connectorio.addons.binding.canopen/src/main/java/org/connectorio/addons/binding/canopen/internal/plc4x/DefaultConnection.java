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
package org.connectorio.addons.binding.canopen.internal.plc4x;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.apache.plc4x.java.spi.values.PlcStruct;
import org.connectorio.addons.binding.canopen.api.CoConnection;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.api.CoSubscription;
import org.connectorio.plc4x.extras.decorator.CompositeDecorator;
import org.connectorio.plc4x.extras.decorator.DecoratorConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConnection implements CoConnection {

  private final Logger logger = LoggerFactory.getLogger(DefaultConnection.class);

  protected final DecoratorConnection connection;
  private final Map<Integer, DefaultNode> nodes = new ConcurrentHashMap<>();
  private final Map<Integer, List<CoSubscription>> subscriptions = new ConcurrentHashMap<>();
  private final int clientId;

  public DefaultConnection(int clientId, PlcConnection connection, CompositeDecorator decorator) {
    this.clientId = clientId;
    this.connection = new DecoratorConnection(connection, decorator, decorator, decorator, decorator);
  }

  @Override
  public CoNode getNode(int nodeId) {
    return nodes.computeIfAbsent(nodeId, (id) -> new DefaultNode(this, id));
  }

  @Override
  public CoNode getLocalNode() {
    return getNode(clientId);
  }

  public CompletableFuture<CoSubscription> subscribe(int nodeId, CANOpenService service, Consumer<byte[]> consumer) {
    PlcSubscriptionRequest request = connection.subscriptionRequestBuilder()
      .addEventField("subscribe", service.name() + ":" + nodeId + ":" + CANOpenDataType.RECORD).build();

    return request.execute().thenApply(sub -> {
      if (!subscriptions.containsKey(nodeId)) {
        subscriptions.put(nodeId, new ArrayList<>());
      }
      DefaultSubscription<byte[]> subscribe = new DefaultSubscription<>(connection, sub.getSubscriptionHandle("subscribe"), consumer,
        new CoRecordReader("subscribe"));
      subscriptions.get(nodeId).add(subscribe);
      return subscribe;
    });
  }

  @Override
  public CompletableFuture<CoSubscription> heartbeat(int nodeId, Consumer<PlcStruct> consumer) {
    PlcSubscriptionRequest request = connection.subscriptionRequestBuilder()
      .addEventField("subscribe", CANOpenService.HEARTBEAT + ":" + nodeId).build();

    return request.execute().thenApply(sub -> {
      if (!subscriptions.containsKey(nodeId)) {
        subscriptions.put(nodeId, new ArrayList<>());
      }
      DefaultSubscription<PlcStruct> subscribe = new DefaultSubscription<>(connection, sub.getSubscriptionHandle("subscribe"), consumer,
        new CoStructReader("subscribe"));
      subscriptions.get(nodeId).add(subscribe);
      return subscribe;
    });
  }

  @Override
  public void send(int nodeId, CANOpenService service, PlcValue value) {
    connection.writeRequestBuilder().addItem("pdo", service.name() + ":" + nodeId + ":" + CANOpenDataType.RECORD, value)
      .build().execute().whenComplete(new PdoWriteCallback(nodeId, service, value));
  }

  @Override
  public void close() {
    subscriptions.forEach((node, subscriptions) -> {
      subscriptions.forEach(CoSubscription::unsubscribe);
      logger.debug("Node {} subscriptions closed.", node);
    });
    subscriptions.clear();
    logger.debug("Network subscriptions cleaned up.");

    nodes.values().forEach(CoNode::close);
  }

  public void remove(DefaultNode node) {
    int nodeId = node.getNodeId();
    if (subscriptions.containsKey(nodeId)) {
      subscriptions.get(nodeId).forEach(CoSubscription::unsubscribe);
      logger.debug("Node {} subscriptions {} closed.", node, subscriptions.remove(nodeId));
    }

    if (nodes.containsKey(nodeId)) {
      nodes.remove(nodeId);
      logger.debug("Node {} was shut down.", node);
    }
  }

  public String toString() {
    return "CoNetwork[nodes=" + nodes.keySet() + "]";
  }

}
