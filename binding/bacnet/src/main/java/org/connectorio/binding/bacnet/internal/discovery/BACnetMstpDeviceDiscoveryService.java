package org.connectorio.binding.bacnet.internal.discovery;

import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.MSTP_DEVICE_THING_TYPE;

import java.util.Collections;
import org.code_house.bacnet4j.wrapper.mstp.MstpDevice;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;

public class BACnetMstpDeviceDiscoveryService extends BACnetDeviceDiscoveryService<MstpDevice> implements DiscoveryService {

  private BACnetNetworkBridgeHandler<?> handler;

  public BACnetMstpDeviceDiscoveryService() throws IllegalArgumentException {
    super(Collections.singleton(MSTP_DEVICE_THING_TYPE), 60);
  }

  @Override
  protected void enrich(DiscoveryResultBuilder discoveryResult, MstpDevice device) {
    discoveryResult.withProperty("address", (int) device.getAddress()[0]);
  }

  @Override
  protected ThingUID createThingId(MstpDevice device) {
    return new ThingUID(MSTP_DEVICE_THING_TYPE, device.getNetworkNumber() + "_" + device.getInstanceNumber());
  }

}
