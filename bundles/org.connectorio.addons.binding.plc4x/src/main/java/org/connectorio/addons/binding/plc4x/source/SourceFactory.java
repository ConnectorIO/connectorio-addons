package org.connectorio.addons.binding.plc4x.source;

import java.util.concurrent.ScheduledExecutorService;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.model.PlcTag;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.connectorio.addons.binding.source.sampling.SamplingSource;

public interface SourceFactory {

  <T extends PlcTag> SubscriberSource<T> subscriber(PlcConnection connection);

  <T extends PlcTag> SamplingSource<Plc4xSampler<T>> sampling(ScheduledExecutorService executor);

  <T extends PlcTag> SamplingSource<Plc4xSampler<T>> sampling(ScheduledExecutorService executor, SamplerComposer<Plc4xSampler<T>> composer);

}
