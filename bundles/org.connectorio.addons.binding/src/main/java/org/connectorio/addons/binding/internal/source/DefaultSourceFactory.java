package org.connectorio.addons.binding.internal.source;

import java.util.concurrent.ScheduledExecutorService;
import org.connectorio.addons.binding.internal.source.sampling.DefaultSamplingSource;
import org.connectorio.addons.binding.source.SourceFactory;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.connectorio.addons.binding.source.sampling.Sampler;
import org.connectorio.addons.binding.source.sampling.SamplingSource;
import org.osgi.service.component.annotations.Component;

@Component(property = {"default=true"}, service = SourceFactory.class)
public class DefaultSourceFactory implements SourceFactory {

  @Override
  public <T extends Sampler>  SamplingSource<T> sampling(ScheduledExecutorService executor) {
    return createSamplingSource(executor, null);
  }

  @Override
  public <T extends Sampler> SamplingSource<T> sampling(ScheduledExecutorService executor, SamplerComposer<T> reducer) {
    if (reducer == null) {
      throw new IllegalArgumentException("Reducer should not be null, use other factory method variant instead");
    }
    return createSamplingSource(executor, reducer);
  }

  private static <T extends Sampler>  DefaultSamplingSource<T> createSamplingSource(ScheduledExecutorService executor, SamplerComposer<T> reducer) {
    return new DefaultSamplingSource<>(executor, reducer);
  }



}
