package org.connectorio.binding.plc4x.siemens.internal.handler;

import org.apache.plc4x.java.s7.connection.S7PlcConnection;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.plc4x.shared.handler.SharedPlc4xThingHandler;
import org.connectorio.binding.plc4x.siemens.internal.SiemensBindingConstants;
import org.eclipse.smarthome.core.thing.Thing;

public class SiemensPlcHandler extends SharedPlc4xThingHandler<S7PlcConnection, SiemensNetworkBridgeHandler, PollingConfiguration> {

  public SiemensPlcHandler(Thing thing) {
    super(thing);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return SiemensBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
