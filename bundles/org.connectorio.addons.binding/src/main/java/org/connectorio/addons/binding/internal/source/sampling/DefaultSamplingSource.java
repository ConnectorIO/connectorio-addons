package org.connectorio.addons.binding.internal.source.sampling;

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
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.connectorio.addons.binding.source.sampling.Sampler;
import org.connectorio.addons.binding.source.sampling.SamplingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSamplingSource<T extends Sampler> implements SamplingSource<T> {

  private final Map<Long, Map<String, T>> operations = new ConcurrentHashMap<>();
  private final Set<ScheduledFuture<?>> futures = new CopyOnWriteArraySet<>();

  private final ScheduledExecutorService executor;
  private final SamplerComposer<T> composer;

  public DefaultSamplingSource(ScheduledExecutorService executor, SamplerComposer<T> composer) {
    this.executor = executor;
    this.composer = composer;
  }

  @Override
  public void add(Long interval, String id, T sampler) {
    if (!operations.containsKey(interval)) {
      operations.put(interval, new ConcurrentHashMap<>());
    }
    operations.get(interval).put(id, sampler);
  }

  @Override
  public void request(T task) {
    executor.submit(new SamplerRunnable(task));
  }

  @Override
  public void remove(String id) {
    Long interval = null;
    for (Entry<Long, Map<String, T>> entry : operations.entrySet()) {
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

    for (Entry<Long, Map<String, T>> sampleOperations : operations.entrySet()) {
      Map<String, T> polledValues = sampleOperations.getValue();

      Collection<T> samplers = composer == null ? polledValues.values() : composer.merge(new ArrayList<>(polledValues.values()));
      for (T sampler : samplers) {
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(new SamplerRunnable(sampler),
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

  static class SamplerRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SamplerRunnable.class);

    private final Sampler sampler;

    SamplerRunnable(Sampler sampler) {
      this.sampler = sampler;
    }

    @Override
    public void run() {
      logger.trace("Executing operation {}", sampler);
      sampler.fetch();
    }
  }
}