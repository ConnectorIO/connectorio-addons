package org.connectorio.addons.binding.mbus.internal.handler.source;

import java.util.function.Consumer;
import org.connectorio.addons.binding.mbus.internal.handler.converter.Converter;
import org.openhab.core.types.State;
import org.openmuc.jmbus.DataRecord;

public class MBusRecordCallback implements Consumer<DataRecord> {

  private final Converter converter;
  private final Consumer<State> callback;

  public MBusRecordCallback(Converter converter, Consumer<State> callback) {
    this.converter = converter;
    this.callback = callback;
  }

  @Override
  public void accept(DataRecord dataRecord) {
    converter.toState(dataRecord);
  }

}
