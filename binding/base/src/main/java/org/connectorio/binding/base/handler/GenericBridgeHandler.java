package org.connectorio.binding.base.handler;

import java.util.Optional;
import org.connectorio.binding.base.config.Configuration;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;

public interface GenericBridgeHandler<C extends Configuration> extends BridgeHandler {

  Optional<C> getBridgeConfig();

}
