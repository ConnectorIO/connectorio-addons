package org.connectorio.binding.base.handler.polling.common;

import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.GenericBridgeHandlerBase;
import org.connectorio.binding.base.handler.polling.PollingBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;

public abstract class BasePollingBridgeHandler<C extends PollingConfiguration> extends
  GenericBridgeHandlerBase<C> implements PollingBridgeHandler<C> {

  public BasePollingBridgeHandler(Bridge bridge) {
    super(bridge);
  }

  public Long getRefreshInterval() {
    return getBridgeConfig().map(cfg -> cfg.refreshInterval)
      .filter(interval -> interval != 0)
      .orElseGet(this::getDefaultPollingInterval);
  }

  protected abstract Long getDefaultPollingInterval();

}
