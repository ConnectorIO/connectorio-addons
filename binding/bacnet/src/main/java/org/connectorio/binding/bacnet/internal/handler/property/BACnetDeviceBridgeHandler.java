package org.connectorio.binding.bacnet.internal.handler.property;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.connectorio.binding.bacnet.internal.config.DeviceConfig;
import org.connectorio.binding.bacnet.internal.handler.network.BACnetNetworkBridgeHandler;
import org.connectorio.binding.base.handler.polling.PollingBridgeHandler;

public interface BACnetDeviceBridgeHandler<B extends BACnetNetworkBridgeHandler<?>, C extends DeviceConfig> extends
  PollingBridgeHandler<C> {

  Optional<CompletableFuture<BacNetClient>> getClient();

  Optional<B> getBridgeHandler();

  Device getDevice();

}
