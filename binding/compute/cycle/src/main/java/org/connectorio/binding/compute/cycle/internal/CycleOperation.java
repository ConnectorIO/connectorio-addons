package org.connectorio.binding.compute.cycle.internal;

import org.eclipse.smarthome.core.thing.ChannelUID;

public interface CycleOperation {

  ChannelUID getChannelId();

  void open();

  void close();

}
