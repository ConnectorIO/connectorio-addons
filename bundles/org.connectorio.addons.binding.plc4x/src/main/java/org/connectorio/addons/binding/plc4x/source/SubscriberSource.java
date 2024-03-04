package org.connectorio.addons.binding.plc4x.source;

import java.util.function.Consumer;
import org.apache.plc4x.java.api.model.PlcTag;
import org.connectorio.addons.binding.source.Source;

public interface SubscriberSource<T extends PlcTag> extends Source {

  void add(String id, T tag, Consumer<Object> consumer);

  void remove(String id);

}
