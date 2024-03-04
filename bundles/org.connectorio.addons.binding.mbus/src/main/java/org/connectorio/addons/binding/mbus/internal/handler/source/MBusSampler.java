package org.connectorio.addons.binding.mbus.internal.handler.source;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.source.sampling.Sampler;
import org.openmuc.jmbus.DataRecord;

public interface MBusSampler extends Sampler {

  @Override
  CompletableFuture<List<DataRecord>> fetch();
}
