package org.connectorio.addons.binding.plc4x.internal;

import java.util.concurrent.ScheduledExecutorService;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.model.PlcTag;
import org.connectorio.addons.binding.plc4x.internal.source.DefaultPlc4xSamplingSource;
import org.connectorio.addons.binding.plc4x.internal.source.DefaultSubscriberSource;
import org.connectorio.addons.binding.plc4x.source.Plc4xSampler;
import org.connectorio.addons.binding.plc4x.source.SourceFactory;
import org.connectorio.addons.binding.plc4x.source.SubscriberSource;
import org.connectorio.addons.binding.source.sampling.SamplerComposer;
import org.connectorio.addons.binding.source.sampling.SamplingSource;
import org.osgi.service.component.annotations.Component;

@Component(property = {
  "plc4x=true"
}, service = SourceFactory.class)
public class Plc4xSourceFactory implements SourceFactory {

  @Override
  public <T extends PlcTag> SubscriberSource<T> subscriber(PlcConnection connection) {
    return new DefaultSubscriberSource<>(connection);
  }

  @Override
  public <T extends PlcTag> SamplingSource<Plc4xSampler<T>> sampling(ScheduledExecutorService executor) {
    return new DefaultPlc4xSamplingSource<>(executor, null);
  }

  @Override
  public <T extends PlcTag> SamplingSource<Plc4xSampler<T>> sampling(ScheduledExecutorService executor, SamplerComposer<Plc4xSampler<T>> composer) {
    return new DefaultPlc4xSamplingSource<>(executor, composer);
  }
}
