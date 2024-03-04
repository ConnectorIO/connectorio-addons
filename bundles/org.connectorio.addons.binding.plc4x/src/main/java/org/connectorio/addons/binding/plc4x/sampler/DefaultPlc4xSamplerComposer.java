package org.connectorio.addons.binding.plc4x.sampler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.model.PlcTag;
import org.connectorio.addons.binding.plc4x.source.Plc4xSampler;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;

public class DefaultPlc4xSamplerComposer<T extends PlcTag> implements SamplerComposer<Plc4xSampler<T>> {

  private final PlcConnection connection;

  public DefaultPlc4xSamplerComposer(PlcConnection connection) {
    this.connection = connection;
  }

  @Override
  public List<Plc4xSampler<T>> merge(List<Plc4xSampler<T>> samplers) {
    Map<String, T> tags = new HashMap<>();
    Map<String, Consumer<Object>> callbacks = new HashMap<>();

    for (Plc4xSampler<T> sampler : samplers) {
      Map<String, T> samplerTags = sampler.getTags();
      for (String tag : samplerTags.keySet()) {
        if (tags.containsKey(tag)) {
          throw new IllegalArgumentException("Duplicated tag found: " + tag);
        }
      }
      tags.putAll(samplerTags);
      callbacks.putAll(sampler.getCallbacks());
    }

    return Collections.singletonList(new DefaultPlc4xSampler<>(connection, tags, callbacks));
  }

}
