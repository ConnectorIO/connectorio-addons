package org.connectorio.addons.binding.canbus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface CanConnection {

  CompletableFuture<Void> write(int cob, byte[] data);

  // cancellable subscription
  Future<Void> subscribe(int cob, Consumer<byte[]> callback);

}
