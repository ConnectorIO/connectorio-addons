package org.connectorio.binding.efficiency.internal.memo;

import java.util.function.Consumer;

public interface StateCollector<T> {

  void addStateReceiver(StateReceiver<T> receiver);

  void removeStateReceiver(StateReceiver<T> receiver);

}
