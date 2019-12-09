package org.connectorio.binding.base.handler.polling.common;

import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.GenericThingHandlerBase;
import org.connectorio.binding.base.handler.PollingHandler;
import org.connectorio.binding.base.handler.polling.PollingBridgeHandler;
import org.connectorio.binding.base.handler.polling.PollingThingHandler;
import org.eclipse.smarthome.core.thing.Thing;

public abstract class BasePollingThingHandler<B extends PollingBridgeHandler<?>, C extends PollingConfiguration> extends
    GenericThingHandlerBase<B, C> implements PollingThingHandler<B, C> {

  public BasePollingThingHandler(Thing thing) {
    super(thing);
  }

  public Long getRefreshInterval() {
    return getThingConfig().map(cfg -> cfg.refreshInterval)
      .filter(interval -> interval > 0)
      .orElseGet(() -> getBridgeHandler().map(PollingHandler::getRefreshInterval)
        .orElseGet(this::getDefaultPollingInterval)
      );
  }

  protected abstract Long getDefaultPollingInterval();

}
