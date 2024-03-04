package org.connectorio.addons.binding.source.sampling;

import org.connectorio.addons.binding.source.Source;

public interface SamplingSource<T extends Sampler> extends Source {

  void add(Long interval, String id, T task);

  void remove(String id);

  void request(T task);

  /**
   * Creates unoptimized (linked) operation which just calls second operation after first.
   *
   * @param first First operation to call.
   * @param second Second operation to call.
   * @return Composite operation.
   */
//  CompositeOperation compose(Operation first, Operation second);

}
