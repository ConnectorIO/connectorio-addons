package org.connectorio.addons.binding.internal.source;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.connectorio.addons.binding.source.sampling.Sampler;

public class ChainingComposer implements SamplerComposer<Sampler> {

  @Override
  public List<Sampler> merge(List<Sampler> samplers) {
    return List.of(new ChainedSampler(samplers));
  }

  static class ChainedSampler implements Sampler {

    private final Collection<Sampler> samplers;

    ChainedSampler(Collection<Sampler> samplers) {
      this.samplers = samplers;
    }

    @Override
    public CompletableFuture<?> fetch() {
      CompletableFuture<Object> completedFuture = CompletableFuture.completedFuture(null);
      for (Sampler sampler : samplers) {
        completedFuture = completedFuture.thenCombine(sampler.fetch(), (left, right) -> null);
      }
      return completedFuture;
    }
  }

}
