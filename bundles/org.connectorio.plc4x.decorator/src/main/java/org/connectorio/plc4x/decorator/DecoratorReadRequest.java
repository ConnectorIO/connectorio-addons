package org.connectorio.plc4x.decorator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.model.PlcField;

public class DecoratorReadRequest implements PlcReadRequest {

  private final PlcReadRequest delegate;
  private final ReadDecorator decorator;

  public DecoratorReadRequest(PlcReadRequest delegate, ReadDecorator decorator) {
    this.delegate = delegate;
    this.decorator = decorator;
  }

  @Override
  public CompletableFuture<? extends PlcReadResponse> execute() {
    return decorator.decorateReadResponse(this, delegate.execute());
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
  public PlcField getField(String name) {
    return delegate.getField(name);
  }

  @Override
  public List<PlcField> getFields() {
    return delegate.getFields();
  }
}