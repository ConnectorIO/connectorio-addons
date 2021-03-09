package org.connectorio.plc4x.decorator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadRequest.Builder;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;

public class CompositeDecorator implements ReadDecorator, WriteDecorator, SubscribeDecorator, UnsubscribeDecorator {

  private final List<ReadDecorator> readDecorators = new CopyOnWriteArrayList<>();
  private final List<WriteDecorator> writeDecorators = new CopyOnWriteArrayList<>();
  private final List<SubscribeDecorator> subscribeDecorators = new CopyOnWriteArrayList<>();
  private final List<UnsubscribeDecorator> unsubscribeDecorators = new CopyOnWriteArrayList<>();

  @Override
  public Builder decorateRead(final Builder delegate) {
    Builder decorated = delegate;
    for (ReadDecorator decorator : readDecorators) {
      decorated = decorator.decorateRead(decorated);
    }
    return decorated;
  }

  @Override
  public PlcReadRequest decorateReadRequest(PlcReadRequest delegate) {
    PlcReadRequest decorated = delegate;
    for (ReadDecorator decorator : readDecorators) {
      decorated = decorator.decorateReadRequest(decorated);
    }
    return decorated;
  }

  @Override
  public CompletableFuture<? extends PlcReadResponse> decorateReadResponse(DecoratorReadRequest request, CompletableFuture<? extends PlcReadResponse> response) {
    CompletableFuture<? extends PlcReadResponse> decorated = response;
    for (ReadDecorator decorator : readDecorators) {
      decorated = decorator.decorateReadResponse(request, decorated);
    }
    return decorated;
  }

  @Override
  public PlcWriteRequest.Builder decorateWrite(PlcWriteRequest.Builder delegate) {
    PlcWriteRequest.Builder decorated = delegate;
    for (WriteDecorator decorator : writeDecorators) {
      decorated = decorator.decorateWrite(decorated);
    }
    return decorated;
  }

  @Override
  public PlcWriteRequest decorateWriteRequest(PlcWriteRequest delegate) {
    PlcWriteRequest decorated = delegate;
    for (WriteDecorator decorator : writeDecorators) {
      decorated = decorator.decorateWriteRequest(decorated);
    }
    return decorated;
  }

  @Override
  public CompletableFuture<? extends PlcWriteResponse> decorateWriteResponse(DecoratorWriteRequest request, CompletableFuture<? extends PlcWriteResponse> response) {
    CompletableFuture<? extends PlcWriteResponse> decorated = response;
    for (WriteDecorator decorator : writeDecorators) {
      decorated = decorator.decorateWriteResponse(request, decorated);
    }
    return decorated;
  }

  @Override
  public PlcSubscriptionRequest.Builder decorateSubscribe(PlcSubscriptionRequest.Builder delegate) {
    PlcSubscriptionRequest.Builder decorated = delegate;
    for (SubscribeDecorator decorator : subscribeDecorators) {
      decorated = decorator.decorateSubscribe(decorated);
    }
    return decorated;
  }

  @Override
  public PlcSubscriptionRequest decorateSubscribeRequest(PlcSubscriptionRequest delegate) {
    PlcSubscriptionRequest decorated = delegate;
    for (SubscribeDecorator decorator : subscribeDecorators) {
      decorated = decorator.decorateSubscribeRequest(decorated);
    }
    return decorated;
  }

  @Override
  public CompletableFuture<? extends PlcSubscriptionResponse> decorateSubscribeResponse(DecoratorSubscriptionRequest request, CompletableFuture<? extends PlcSubscriptionResponse> response) {
    CompletableFuture<? extends PlcSubscriptionResponse> decorated = response;
    for (SubscribeDecorator decorator : subscribeDecorators) {
      decorated = decorator.decorateSubscribeResponse(request, decorated);
    }
    return decorated;
  }

  @Override
  public PlcUnsubscriptionRequest.Builder decorateUnsubscribe(PlcUnsubscriptionRequest.Builder delegate) {
    PlcUnsubscriptionRequest.Builder decorated = delegate;
    for (UnsubscribeDecorator decorator : unsubscribeDecorators) {
      decorated = decorator.decorateUnsubscribe(decorated);
    }
    return decorated;
  }

  @Override
  public PlcUnsubscriptionRequest decorateUnsubscribeRequest(PlcUnsubscriptionRequest delegate) {
    PlcUnsubscriptionRequest decorated = delegate;
    for (UnsubscribeDecorator decorator : unsubscribeDecorators) {
      decorated = decorator.decorateUnsubscribeRequest(decorated);
    }
    return decorated;
  }

  @Override
  public CompletableFuture<PlcUnsubscriptionResponse> decorateUnsubscribeResponse(PlcUnsubscriptionRequest request, CompletableFuture<PlcUnsubscriptionResponse> response) {
    CompletableFuture<PlcUnsubscriptionResponse> decorated = response;
    for (UnsubscribeDecorator decorator : unsubscribeDecorators) {
      decorated = decorator.decorateUnsubscribeResponse(request, decorated);
    }
    return decorated;
  }

  public void add(Object decorator) {
    if (decorator instanceof ReadDecorator) {
      readDecorators.add((ReadDecorator) decorator);
    }
    if (decorator instanceof WriteDecorator) {
      writeDecorators.add((WriteDecorator) decorator);
    }
    if (decorator instanceof SubscribeDecorator) {
      subscribeDecorators.add((SubscribeDecorator) decorator);
    }
    if (decorator instanceof UnsubscribeDecorator) {
      unsubscribeDecorators.add((UnsubscribeDecorator) decorator);
    }
  }
}
