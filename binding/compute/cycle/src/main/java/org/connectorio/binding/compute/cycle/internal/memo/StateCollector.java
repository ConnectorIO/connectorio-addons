package org.connectorio.binding.compute.cycle.internal.memo;

public interface StateCollector<T> {

  void addStateReceiver(StateReceiver<T> receiver);

  void removeStateReceiver(StateReceiver<T> receiver);

}
