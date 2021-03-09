package org.connectorio.plc4x.decorator;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcInvalidFieldException;
import org.apache.plc4x.java.api.messages.PlcBrowseRequest;
import org.apache.plc4x.java.api.messages.PlcReadRequest.Builder;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.metadata.PlcConnectionMetadata;
import org.apache.plc4x.java.api.model.PlcField;
import org.connectorio.plc4x.decorator.noop.NoopReadDecorator;
import org.connectorio.plc4x.decorator.noop.NoopSubscribeDecorator;
import org.connectorio.plc4x.decorator.noop.NoopUnsubscribeDecorator;
import org.connectorio.plc4x.decorator.noop.NoopWriteDecorator;

public class DecoratorConnection implements PlcConnection {

  private final PlcConnection delegate;

  private final ReadDecorator readDecorator;
  private final WriteDecorator writeDecorator;
  private final SubscribeDecorator subscribeDecorator;
  private final UnsubscribeDecorator unsubscribeDecorator;

  public DecoratorConnection(PlcConnection delegate, ReadDecorator readDecorator, WriteDecorator writeDecorator,
    SubscribeDecorator subscribeDecorator, UnsubscribeDecorator unsubscribeDecorator) {
    this.delegate = delegate;
    this.readDecorator = readDecorator == null ? new NoopReadDecorator() : readDecorator;
    this.writeDecorator = writeDecorator == null ? new NoopWriteDecorator() : writeDecorator;
    this.subscribeDecorator = subscribeDecorator == null ? new NoopSubscribeDecorator() : subscribeDecorator;
    this.unsubscribeDecorator = unsubscribeDecorator == null ? new NoopUnsubscribeDecorator() : unsubscribeDecorator;
  }

  @Override
  public void connect() throws PlcConnectionException {
    delegate.connect();
  }

  @Override
  public boolean isConnected() {
    return delegate.isConnected();
  }

  @Override
  public void close() throws Exception {
    delegate.close();
  }

  @Override
  @Deprecated
  public PlcField prepareField(String fieldQuery) throws PlcInvalidFieldException {
    return delegate.prepareField(fieldQuery);
  }

  @Override
  public PlcConnectionMetadata getMetadata() {
    return delegate.getMetadata();
  }

  @Override
  public CompletableFuture<Void> ping() {
    return delegate.ping();
  }

  @Override
  public Builder readRequestBuilder() {
    return readDecorator.decorateRead(delegate.readRequestBuilder());
  }

  @Override
  public PlcWriteRequest.Builder writeRequestBuilder() {
    return writeDecorator.decorateWrite(delegate.writeRequestBuilder());
  }

  @Override
  public PlcSubscriptionRequest.Builder subscriptionRequestBuilder() {
    return new DecoratorSubscriptionRequestBuilder(delegate.subscriptionRequestBuilder(), subscribeDecorator);
  }

  @Override
  public PlcUnsubscriptionRequest.Builder unsubscriptionRequestBuilder() {
    return new DecoratorUnsubscriptionRequestBuilder(delegate.unsubscriptionRequestBuilder(), unsubscribeDecorator);
  }

  @Override
  public PlcBrowseRequest.Builder browseRequestBuilder() {
    return delegate.browseRequestBuilder();
  }

}
