package org.connectorio.binding.base.handler;

import java.util.Optional;
import org.connectorio.binding.base.config.Configuration;
import org.connectorio.binding.base.GenericTypeUtil;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;

public abstract class GenericBridgeHandlerBase<C extends Configuration> extends
    BaseBridgeHandler implements GenericBridgeHandler<C> {

  private final Class<C> configType;

  public GenericBridgeHandlerBase(Bridge bridge) {
    super(bridge);
    this.configType = GenericTypeUtil.<C>resolveTypeVariable("C", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve config type"));
  }

  @Override
  public Optional<C> getBridgeConfig() {
    return Optional.ofNullable(super.getConfigAs(configType));
  }

}
