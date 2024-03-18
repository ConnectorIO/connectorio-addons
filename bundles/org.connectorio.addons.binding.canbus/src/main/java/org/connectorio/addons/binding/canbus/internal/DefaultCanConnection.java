package org.connectorio.addons.binding.canbus.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.can.generic.tag.GenericCANTag;
import org.apache.plc4x.java.genericcan.readwrite.GenericCANDataType;
import org.apache.plc4x.java.spi.values.PlcRawByteArray;
import org.apache.plc4x.java.spi.values.PlcUSINT;
import org.apache.plc4x.java.spi.values.PlcValues;
import org.connectorio.addons.binding.canbus.CanConnection;

public class DefaultCanConnection implements CanConnection {

  private final PlcConnection connection;

  public DefaultCanConnection(PlcConnection connection) {
    this.connection = connection;
  }

  @Override
  public CompletableFuture<Void> write(int cob, byte[] data) {
    GenericCANTag tag = new GenericCANTag(cob, GenericCANDataType.UNSIGNED8, 8);
    return connection.writeRequestBuilder()
      .addTag("data", tag, encode(data))
      .build().execute()
      .thenApply(r -> null);
  }

  private PlcValue encode(byte[] data) {
    return new PlcRawByteArray(data);
  }

  @Override
  public Future<Void> subscribe(int cob, Consumer<byte[]> callback) {
    GenericCANTag tag = new GenericCANTag(cob, GenericCANDataType.UNSIGNED8, 8);

    final AtomicReference<PlcConsumerRegistration> registration = new AtomicReference<>();
    CompletableFuture<Void> ret = new CompletableFuture<>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        PlcConsumerRegistration reg = registration.get();
        if (reg != null) {
          reg.unregister();
        }
        return super.cancel(mayInterruptIfRunning);
      }
    };

    connection.subscriptionRequestBuilder()
      .addEventTag("data", tag)
      .build().execute().thenApply(r -> {
          PlcConsumerRegistration reg = r.getSubscriptionHandle("data")
            .register(new Consumer<PlcSubscriptionEvent>() {
              @Override
              public void accept(PlcSubscriptionEvent plcSubscriptionEvent) {
                Collection<Short> data = plcSubscriptionEvent.getAllShorts("data");
                callback.accept(decode(new ArrayList<>(data)));
              }
            });
          registration.set(reg);
          return registration;
        });

    return ret;
  }

  private byte[] decode(List<Short> data) {
    byte[] bytes = new byte[data.size()];
    for (int index = 0; index < bytes.length; index++) {
      bytes[index] = data.get(index).byteValue();
    }
    return bytes;
  }
}
