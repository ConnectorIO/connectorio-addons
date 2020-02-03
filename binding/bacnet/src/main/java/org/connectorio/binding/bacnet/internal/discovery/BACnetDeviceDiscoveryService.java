package org.connectorio.binding.bacnet.internal.discovery;

import static org.connectorio.binding.bacnet.internal.BACnetBindingConstants.DEVICE_THING_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.api.DeviceDiscoveryListener;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.util.HexUtils;

public class BACnetDeviceDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {

  private BACnetNetworkBridgeHandler<?> handler;

  public BACnetDeviceDiscoveryService() throws IllegalArgumentException {
    super(Collections.singleton(DEVICE_THING_TYPE), 60);
  }

  @Override
  public boolean isBackgroundDiscoveryEnabled() {
    return true;
  }

  @Override
  protected void startBackgroundDiscovery() {
    startScan();
  }

  @Override
  protected void startScan() {
    handler.getClient().thenAcceptAsync(cli -> {
      Set<Device> devices = cli.discoverDevices(TimeUnit.SECONDS.toMillis(getScanTimeout()));
      devices.forEach(this::toDiscoveryResult);
    }, scheduler);
  }

  private void toDiscoveryResult(Device device) {
    DiscoveryResult discoveryResult = DiscoveryResultBuilder
      .create(new ThingUID(DEVICE_THING_TYPE, device.getNetworkNumber() + "_" + device.getInstanceNumber()))
      .withLabel(device.getModelName() + ", " + device.getName() + " (" + device.getVendorName() + ")" + device.getModelName())
      .withBridge(handler.getThing().getUID())
      .withProperty("instance", device.getInstanceNumber())
      .withProperty("address", HexUtils.bytesToHex(device.getAddress()))
      .withRepresentationProperty("address")
      .build();

    thingDiscovered(discoveryResult);
  }

  @Override
  public void setThingHandler(ThingHandler handler) {
    if (handler instanceof BACnetNetworkBridgeHandler) {
      this.handler = (BACnetNetworkBridgeHandler<?>) handler;
    }
  }

  @Override
  public ThingHandler getThingHandler() {
    return this.handler;
  }

  @Override
  public void activate() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
    super.activate(properties);
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

}
