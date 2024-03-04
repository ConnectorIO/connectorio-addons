package org.connectorio.addons.binding.plc4x.source;

import java.util.Map;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.model.PlcTag;
import org.connectorio.addons.binding.source.sampling.Sampler;

public interface Plc4xSampler<T extends PlcTag> extends Sampler {

  Map<String, T> getTags();

  Map<String, Consumer<Object>> getCallbacks();

}
