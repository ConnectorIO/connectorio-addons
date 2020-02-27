package org.connectorio.binding.bacnet.internal.handler.property;

import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.mstp.MstpDevice;
import org.connectorio.binding.bacnet.internal.config.MstpDeviceConfig;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;

public class BACnetMstpDeviceHandler extends BACnetDeviceHandler<MstpDeviceConfig> {

  /**
   * Creates a new instance of this class for the {@link Thing}.
   *
   * @param bridge the thing that should be handled, not null
   */
  public BACnetMstpDeviceHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  protected Device createDevice(MstpDeviceConfig config, Integer networkNumber) {
    return new MstpDevice(config.instance, new byte[] { (byte) config.address }, networkNumber);
  }
}
