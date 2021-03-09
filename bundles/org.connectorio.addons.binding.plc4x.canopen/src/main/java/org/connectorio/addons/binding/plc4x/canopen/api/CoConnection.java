package org.connectorio.addons.binding.plc4x.canopen.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;

public interface CoConnection {

  CoNode getNode(int nodeId);

  CompletableFuture<CoSubscription> subscribe(int nodeId, CANOpenService service, Consumer<byte[]> consumer);

  void close();

  void send(int nodeId, CANOpenService service, PlcValue value);

}
