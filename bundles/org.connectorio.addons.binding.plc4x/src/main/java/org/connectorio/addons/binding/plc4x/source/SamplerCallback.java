package org.connectorio.addons.binding.plc4x.source;

import java.util.function.Consumer;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

public class SamplerCallback implements Consumer<Object> {

  private final Converter converter;
  private final Consumer<State> callback;

  public SamplerCallback(Converter converter, Consumer<State> callback) {
    this.converter = converter;
    this.callback = callback;
  }

  @Override
  public void accept(Object value) {
    State state = converter.fromValue(value);
    if (state != null) {
      callback.accept(state);
    }
  }
}
