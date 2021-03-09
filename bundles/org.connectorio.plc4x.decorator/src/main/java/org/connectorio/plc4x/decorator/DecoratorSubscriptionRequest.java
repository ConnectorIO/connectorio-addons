package org.connectorio.plc4x.decorator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.model.PlcSubscriptionField;

public class DecoratorSubscriptionRequest implements PlcSubscriptionRequest {

  private final PlcSubscriptionRequest delegate;
  private final SubscribeDecorator decorator;

  public DecoratorSubscriptionRequest(PlcSubscriptionRequest delegate, SubscribeDecorator decorator) {
    this.delegate = delegate;
    this.decorator = decorator;
  }

  @Override
  public CompletableFuture<? extends PlcSubscriptionResponse> execute() {
    return decorator.decorateSubscribeResponse(this, delegate.execute());
  }

  @Override
  public int getNumberOfFields() {
    return delegate.getNumberOfFields();
  }

  @Override
  public LinkedHashSet<String> getFieldNames() {
    return delegate.getFieldNames();
  }

  @Override
  public PlcSubscriptionField getField(String name) {
    return delegate.getField(name);
  }

  @Override
  public List<PlcSubscriptionField> getFields() {
    return delegate.getFields();
  }

}
