package org.connectorio.addons.binding.source.sampling;

import java.util.List;

public interface SamplerComposer<T extends Sampler> {

  List<T> merge(List<T> samplers);

}
