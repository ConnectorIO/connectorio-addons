package org.connectorio.addons.binding.source;

import java.util.concurrent.ScheduledExecutorService;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.connectorio.addons.binding.source.sampling.Sampler;
import org.connectorio.addons.binding.source.sampling.SamplingSource;

public interface SourceFactory {

   <T extends Sampler> SamplingSource<T> sampling(ScheduledExecutorService executor);
   <T extends Sampler> SamplingSource<T> sampling(ScheduledExecutorService executor, SamplerComposer<T> reducer);

}
