package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.channel;

import org.connectorio.addons.binding.plc4x.canopen.ta.internal.config.InputOutputObjectConfig;
import org.connectorio.addons.binding.plc4x.canopen.ta.internal.type.TAUnit;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.ValueCallback;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;
import org.openhab.core.types.Command;

public interface ChannelHandler<T extends Value<?>, U extends TAUnit, C extends InputOutputObjectConfig> extends ValueCallback<T> {

  void initialize();
  void dispose();

  void handleCommand(Command command);

}
