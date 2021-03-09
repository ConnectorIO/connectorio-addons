package org.connectorio.addons.binding.plc4x.canopen.internal.plc4x;

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
import org.connectorio.addons.binding.plc4x.canopen.api.CoConnection;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.api.CoSubscription;
import org.connectorio.plc4x.decorator.CompositeDecorator;
import org.connectorio.plc4x.decorator.DecoratorConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConnection implements CoConnection {

  private final Logger logger = LoggerFactory.getLogger(DefaultConnection.class);

  protected final DecoratorConnection connection;
  private final Map<Integer, DefaultNode> nodes = new ConcurrentHashMap<>();
  private final Map<Integer, List<CoSubscription>> subscriptions = new ConcurrentHashMap<>();

  public DefaultConnection(PlcConnection connection, CompositeDecorator decorator) {
    this.connection = new DecoratorConnection(connection, decorator, decorator, decorator, decorator);
  }

  @Override
  public CoNode getNode(int nodeId) {
    return nodes.computeIfAbsent(nodeId, (id) -> new DefaultNode(this, id));
  }

  public CompletableFuture<CoSubscription> subscribe(int nodeId, CANOpenService service, Consumer<byte[]> consumer) {
    PlcSubscriptionRequest request = connection.subscriptionRequestBuilder()
      .addEventField("subscribe", service.name() + ":" + nodeId + ":" + CANOpenDataType.RECORD).build();

    return request.execute().thenApply(sub -> {
      if (!subscriptions.containsKey(nodeId)) {
        subscriptions.put(nodeId, new ArrayList<>());
      }
      DefaultSubscription subscribe = new DefaultSubscription(connection, sub.getSubscriptionHandle("subscribe"), consumer);
      subscriptions.get(nodeId).add(subscribe);
      return subscribe;
    });
  }

  @Override
  public void send(int nodeId, CANOpenService service, PlcValue value) {
    connection.writeRequestBuilder().addItem("pdo", service.name() + ":" + nodeId + ":" + CANOpenDataType.RECORD, value)
      .build().execute().whenComplete((response, error) -> {
      logger.info("Dispatched PDO node {}, service {}, cob {}, data {}", nodeId, service, Integer.toHexString(service.getMin() + nodeId), value, error);
    });
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
