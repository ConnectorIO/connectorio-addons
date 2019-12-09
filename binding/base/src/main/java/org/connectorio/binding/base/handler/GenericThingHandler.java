package org.connectorio.binding.base.handler;

import java.util.Optional;
import org.connectorio.binding.base.config.Configuration;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

public interface GenericThingHandler<B extends BridgeHandler, C extends Configuration> extends ThingHandler {

  Optional<C> getThingConfig();

  Optional<B> getBridgeHandler();
}
