package org.connectorio.binding.bacnet.internal.handler.property;

import com.serotonin.bacnet4j.obj.DeviceObject;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.connectorio.binding.bacnet.internal.config.DeviceConfig;
import org.connectorio.binding.bacnet.internal.discovery.BACnetPropertyDiscoveryService;
import org.connectorio.binding.bacnet.internal.handler.BACnetObjectHandler;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.util.HexUtils;

public class BACnetDeviceHandler extends BACnetObjectHandler<DeviceObject, BACnetNetworkBridgeHandler<?>, DeviceConfig>
  implements BACnetDeviceBridgeHandler<BACnetNetworkBridgeHandler<?>, DeviceConfig> {

  private Device device;

  /**
   * Creates a new instance of this class for the {@link Thing}.
   *
   * @param bridge the thing that should be handled, not null
   */
  public BACnetDeviceHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<BACnetNetworkBridgeHandler<?>> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(BACnetNetworkBridgeHandler.class::isInstance)
      .map(BACnetNetworkBridgeHandler.class::cast);
  }

  @Override
  public void initialize() {
    device = getBridgeConfig()
      .map(cfg -> {
        Integer networkNumber = getBridgeHandler().flatMap(BACnetNetworkBridgeHandler::getNetworkNumber).orElse(0);
        return new Device(cfg.instance, HexUtils.hexToBytes(cfg.address), networkNumber);
      }).orElse(null);

    if (device != null) {
      updateStatus(ThingStatus.ONLINE);
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing device configuration");
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(BACnetPropertyDiscoveryService.class);
  }

  @Override
  public Optional<CompletableFuture<BacNetClient>> getClient() {
    return getBridgeHandler().map(BACnetNetworkBridgeHandler::getClient);
  }

  @Override
  public Device getDevice() {
    return device;
  }

}
