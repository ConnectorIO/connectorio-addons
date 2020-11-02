package org.connectorio.binding.plc4x.canopen.internal.handler;

import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.binding.base.handler.polling.common.BasePollingBridgeHandler;
import org.connectorio.binding.plc4x.canopen.CANopenBindingConstants;
import org.connectorio.binding.plc4x.canopen.internal.config.SDOConfig;
import org.connectorio.binding.plc4x.shared.handler.base.PollingPlc4xThingHandler;
import org.eclipse.smarthome.core.thing.Thing;

public class SDOThingHandler extends PollingPlc4xThingHandler<PlcConnection, CANOpenGenericBridgeHandler, SDOConfig> {

  public SDOThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    super.initialize();
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return getBridgeHandler().map(BasePollingBridgeHandler::getRefreshInterval)
      .orElse(CANopenBindingConstants.DEFAULT_POLLING_INTERVAL);
  }

}
