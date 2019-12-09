package org.connectorio.binding.base.handler;

import java.util.Optional;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.GenericTypeUtil;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;

public abstract class GenericThingHandlerBase<B extends BridgeHandler, C extends PollingConfiguration> extends
    BaseThingHandler implements GenericThingHandler<B, C> {

  private final Class<B> bridgeType;
  private final Class<C> configType;

  public GenericThingHandlerBase(Thing thing) {
    super(thing);
    this.bridgeType = GenericTypeUtil.<B>resolveTypeVariable("B", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve bridge type"));
    this.configType = GenericTypeUtil.<C>resolveTypeVariable("C", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve config type"));
  }

  @Override
  public Optional<C> getThingConfig() {
    return Optional.ofNullable(super.getConfigAs(configType));
  }

  @Override
  public Optional<B> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(bridgeType::isInstance)
      .map(handler -> (B) handler);
  }

//  protected final Class<B> getBridgeType() {
//    return bridgeType;
//  }

}
