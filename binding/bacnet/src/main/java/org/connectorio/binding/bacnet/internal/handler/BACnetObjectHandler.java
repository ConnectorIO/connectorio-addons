package org.connectorio.binding.bacnet.internal.handler;

import com.serotonin.bacnet4j.obj.BACnetObject;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.binding.bacnet.internal.config.BACnetConfig;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.connectorio.binding.base.handler.polling.common.BasePollingBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;

public abstract class BACnetObjectHandler<T extends BACnetObject, B extends BACnetNetworkBridgeHandler<?>, C extends BACnetConfig>
  extends BasePollingBridgeHandler<C> {

  public BACnetObjectHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

}
