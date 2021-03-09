package org.connectorio.addons.binding.plc4x.canopen.internal.plc4x;

import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.connectorio.addons.binding.plc4x.canopen.api.CoSubscription;

public class DefaultSubscription implements CoSubscription {

  private final PlcConnection connection;
  private final PlcSubscriptionHandle subscribe;
  private final Consumer<byte[]> consumer;
  private final PlcConsumerRegistration registration;

  public DefaultSubscription(PlcConnection connection, PlcSubscriptionHandle subscribe, Consumer<byte[]> consumer) {
    this.connection = connection;
    this.subscribe = subscribe;
    this.consumer = consumer;
    registration = subscribe.register(new Consumer<PlcSubscriptionEvent>() {
      @Override
      public void accept(PlcSubscriptionEvent event) {
        consumer.accept(ResultReader.getBytes(event, "subscribe"));
      }
    });
  }

  @Override
  public void close() throws Exception {
    unsubscribe();
  }

  @Override
  public void unsubscribe() {
    registration.unregister();
  }

  public String toString() {
    return "CoSubscription [" + subscribe + "]";
  }

}
