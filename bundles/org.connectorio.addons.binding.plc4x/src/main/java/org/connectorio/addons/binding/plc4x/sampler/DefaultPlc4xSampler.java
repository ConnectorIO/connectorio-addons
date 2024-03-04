package org.connectorio.addons.binding.plc4x.sampler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest.Builder;
import org.apache.plc4x.java.api.model.PlcTag;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.connectorio.addons.binding.plc4x.source.Plc4xSampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultPlc4xSampler<T extends PlcTag> implements Plc4xSampler<T> {

  private final Logger logger = LoggerFactory.getLogger(DefaultPlc4xSampler.class);
  private final PlcConnection connection;
  private final Map<String, T> tags;
  private final Map<String, Consumer<Object>> callbacks;

  public DefaultPlc4xSampler(PlcConnection connection, String id, T tag, Consumer<Object> callback) {
    this(connection, Map.of(id, tag), Map.of(id, callback));
  }

  public DefaultPlc4xSampler(PlcConnection connection, Map<String, T> tags, Map<String, Consumer<Object>> callbacks) {
    this.connection = connection;
    this.tags = tags;
    this.callbacks = callbacks;
  }

  @Override
  public Map<String, T> getTags() {
    return tags;
  }

  @Override
  public Map<String, Consumer<Object>> getCallbacks() {
    return callbacks;
  }

  @Override
  public CompletableFuture<?> fetch() {
    Builder requestBuilder = connection.readRequestBuilder();
    for (Entry<String, T> entry : tags.entrySet()) {
      requestBuilder.addTag(entry.getKey(), entry.getValue());
    }
    return requestBuilder.build().execute().whenComplete((response, error) -> {
      if (error != null) {
        logger.warn("Could not retrieve tag {} values", tags, error);
        return;
      }
      for (String tag : response.getTagNames()) {
        PlcResponseCode code = response.getResponseCode(tag);
        Object value = response.getObject(tag);
        if (code != PlcResponseCode.OK) {
          logger.warn("Received non-OK status {} for tag {}, ignoring its value {}", code, tag, value);
          return;
        }
        if (value != null) {
          callbacks.get(tag).accept(value);
        }
      }
    });
  }

}
