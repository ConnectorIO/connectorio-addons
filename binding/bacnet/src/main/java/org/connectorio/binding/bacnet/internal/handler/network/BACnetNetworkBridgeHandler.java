package org.connectorio.binding.bacnet.internal.handler.network;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.polling.PollingBridgeHandler;

public interface BACnetNetworkBridgeHandler<C extends PollingConfiguration> extends PollingBridgeHandler<C> {

  CompletableFuture<BacNetClient> getClient();

  Optional<Integer> getNetworkNumber();

}
