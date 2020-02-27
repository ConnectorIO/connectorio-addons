package org.connectorio.binding.bacnet.internal.handler.property;

import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.ip.IpDevice;
import org.connectorio.binding.bacnet.internal.config.IpDeviceConfig;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;

public class BACnetIpDeviceHandler extends BACnetDeviceHandler<IpDeviceConfig> {

  /**
   * Creates a new instance of this class for the {@link Thing}.
   *
   * @param bridge the thing that should be handled, not null
   */
  public BACnetIpDeviceHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  protected Device createDevice(IpDeviceConfig config, Integer networkNumber) {
    return new IpDevice(config.instance, config.address, config.port, networkNumber);
  }
}
