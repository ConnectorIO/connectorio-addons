package org.connectorio.binding.bacnet.internal.discovery;

import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.IP_DEVICE_THING_TYPE;

import java.util.Collections;
import org.code_house.bacnet4j.wrapper.ip.IpDevice;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;

public class BACnetIpDeviceDiscoveryService extends BACnetDeviceDiscoveryService<IpDevice> implements DiscoveryService {

  private BACnetNetworkBridgeHandler<?> handler;

  public BACnetIpDeviceDiscoveryService() throws IllegalArgumentException {
    super(Collections.singleton(IP_DEVICE_THING_TYPE), 60);
  }

  @Override
  protected void enrich(DiscoveryResultBuilder discoveryResult, IpDevice device) {
    discoveryResult.withProperty("address", device.getHostAddress())
      .withProperty("port", device.getPort());
  }

  @Override
  protected ThingUID createThingId(IpDevice device) {
    return new ThingUID(IP_DEVICE_THING_TYPE, device.getNetworkNumber() + "_" + device.getInstanceNumber());
  }

}
