package org.connectorio.binding.plc4x.shared.handler;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.polling.common.BasePollingBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;

public abstract class SharedPlc4xBridgeHandler<T extends PlcConnection, C extends PollingConfiguration> extends
    BasePollingBridgeHandler<C> implements BridgeHandler {

  public SharedPlc4xBridgeHandler(Bridge bridge) {
    super(bridge);
  }

  public abstract T getConnection();

  public abstract CompletableFuture<T> getInitializer();

  protected String hostWithPort(String host, Integer port) {
    return host + (port == null ? "" : ":" + port);
  }

}
