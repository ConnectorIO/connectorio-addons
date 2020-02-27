package org.connectorio.binding.bacnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.connectorio.binding.base.GenericTypeUtil;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;

public abstract class BACnetDeviceDiscoveryService<T extends Device> extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {

  private BACnetNetworkBridgeHandler<?> handler;
  private final Class<T> type;

  public BACnetDeviceDiscoveryService(Set<ThingTypeUID> supportedThingsTypes, int timeout) throws IllegalArgumentException {
    super(supportedThingsTypes, timeout);
    this.type = GenericTypeUtil.<T>resolveTypeVariable("T", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve discovery device type"));
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
    if (!type.isAssignableFrom(device.getClass())) {
      return;
    }

    T discoveredDevice = (T) device;

    DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder
      .create(createThingId(discoveredDevice))
      .withLabel(device.getModelName() + ", " + device.getName() + " (" + device.getVendorName() + ")" + device.getModelName())
      .withBridge(handler.getThing().getUID())
      .withProperty("instance", device.getInstanceNumber())
      .withProperty("network", device.getBacNet4jAddress().getNetworkNumber().intValue())
      .withRepresentationProperty("address");

    enrich(discoveryResult, discoveredDevice);
    thingDiscovered(discoveryResult.build());
  }

  protected abstract void enrich(DiscoveryResultBuilder discoveryResult, T device);

  protected abstract ThingUID createThingId(T device);

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
