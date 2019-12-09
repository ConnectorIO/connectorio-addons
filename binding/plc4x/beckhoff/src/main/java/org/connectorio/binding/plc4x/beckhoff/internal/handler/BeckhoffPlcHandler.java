package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import org.apache.plc4x.java.ads.connection.AdsAbstractPlcConnection;
import org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffBridgeConfiguration;
import org.connectorio.binding.plc4x.shared.handler.SharedPlc4xThingHandler;
import org.eclipse.smarthome.core.thing.Thing;

public class BeckhoffPlcHandler extends SharedPlc4xThingHandler<AdsAbstractPlcConnection, BeckhoffBridgeHandler<AdsAbstractPlcConnection, ?>, BeckhoffBridgeConfiguration> {

  public BeckhoffPlcHandler(Thing thing) {
    super(thing);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BeckhoffBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
