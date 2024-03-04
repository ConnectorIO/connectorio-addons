package org.connectorio.addons.binding.plc4x.internal.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.plc4x.java.api.model.PlcTag;
import org.connectorio.addons.binding.plc4x.source.Plc4xSampler;
import org.connectorio.addons.binding.source.sampling.Sampler;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.connectorio.addons.binding.source.sampling.SamplingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPlc4xSamplingSource<T extends PlcTag> implements SamplingSource<Plc4xSampler<T>> {

  private final Map<Long, Map<String, Plc4xSampler<T>>> operations = new ConcurrentHashMap<>();
  private final Set<ScheduledFuture<?>> futures = new CopyOnWriteArraySet<>();

  private final ScheduledExecutorService executor;
  private final SamplerComposer<Plc4xSampler<T>> composer;

  public DefaultPlc4xSamplingSource(ScheduledExecutorService executor, SamplerComposer<Plc4xSampler<T>> composer) {
    this.executor = executor;
    this.composer = composer;
  }

  @Override
  public void add(Long interval, String id, Plc4xSampler<T> sampler) {
    if (!operations.containsKey(interval)) {
      operations.put(interval, new ConcurrentHashMap<>());
    }
    operations.get(interval).put(id, sampler);
  }

  @Override
  public void request(Plc4xSampler<T> task) {
    executor.submit(new Plc4xSamplerRunnable(task));
  }

  @Override
  public void remove(String id) {
    Long interval = null;
    for (Entry<Long, Map<String, Plc4xSampler<T>>> entry : operations.entrySet()) {
      if (entry.getValue().remove(id) != null) {
        interval = entry.getKey();
      }
    }

    if (interval != null && operations.get(interval).isEmpty()) {
      operations.remove(interval);
    }
  }

  @Override
  public boolean start() {
    if (operations.isEmpty()) {
      return false;
    }

    for (Entry<Long, Map<String, Plc4xSampler<T>>> sampleOperations : operations.entrySet()) {
      Map<String, Plc4xSampler<T>> polledValues = sampleOperations.getValue();

      Collection<Plc4xSampler<T>> samplers = composer == null ? polledValues.values() : composer.merge(new ArrayList<>(polledValues.values()));
      for (Plc4xSampler<T> sampler : samplers) {
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(new Plc4xSamplerRunnable(sampler),
            sampleOperations.getKey(), sampleOperations.getKey(), TimeUnit.MILLISECONDS);
        futures.add(future);
      }

    }

    return true;
  }

  @Override
  public void stop() {
    futures.forEach((future) -> future.cancel(true));
  }

  static class Plc4xSamplerRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(Plc4xSamplerRunnable.class);

    private final Sampler sampler;

    Plc4xSamplerRunnable(Sampler sampler) {
      this.sampler = sampler;
    }

    @Override
    public void run() {
      logger.trace("Executing operation {}", sampler);
      sampler.fetch();
    }
  }
}
