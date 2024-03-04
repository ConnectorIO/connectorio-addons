package org.connectorio.addons.binding.plc4x.internal.source;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.model.PlcTag;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.connectorio.addons.binding.plc4x.source.SubscriberSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSubscriberSource<T extends PlcTag> implements SubscriberSource<T> {

  private final Logger logger = LoggerFactory.getLogger(DefaultSubscriberSource.class);

  private final Map<String, Consumer<Object>> handlers = new ConcurrentHashMap<>();
  private final PlcSubscriptionRequest.Builder subscribeBuilder;
  private final PlcUnsubscriptionRequest.Builder unsubscribeBuilder;


  public DefaultSubscriberSource(PlcConnection connection) {
    subscribeBuilder = connection.subscriptionRequestBuilder();
    unsubscribeBuilder = connection.unsubscriptionRequestBuilder();
  }

  @Override
  public void add(String id, T tag, Consumer<Object> onChange) {
    subscribeBuilder.addChangeOfStateTag(id, tag);
    handlers.put(id, onChange);
  }

  @Override
  public void remove(String id) {
    handlers.remove(id);
  }

  @Override
  public boolean start() {
    if (handlers.isEmpty()) {
      return false;
    }

    subscribeBuilder.build().execute().whenComplete((response, error) -> {
      if (error != null) {
        return;
      }

      for (String id : response.getTagNames()) {
        PlcSubscriptionHandle subscriptionHandle = response.getSubscriptionHandle(id);
        subscriptionHandle.register(new Consumer<>() {
          @Override
          public void accept(PlcSubscriptionEvent plcSubscriptionEvent) {
            Object value = plcSubscriptionEvent.getObject(id);
            PlcResponseCode code = plcSubscriptionEvent.getResponseCode(id);
            if (code != PlcResponseCode.OK) {
              logger.debug("Received non-OK subscription response code {} for channel {}. Ignoring value {}.", id, code, value);
              return;
            }

            if (value != null) {
              logger.debug("Channel {} received update {}", id, value);
              Consumer<Object> consumer = handlers.get(id);
              if (consumer != null) {
                consumer.accept(value);
              } else {
                logger.warn("Unknown channel/tag association: {}", id);
              }
            }
          }
        });
        unsubscribeBuilder.addHandles(subscriptionHandle);
      }
    });
    return true;
  }

  @Override
  public void stop() {
    unsubscribeBuilder.build().execute().join();
  }

}