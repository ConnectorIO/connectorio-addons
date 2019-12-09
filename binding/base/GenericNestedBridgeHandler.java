package org.connectorio.binding.base.handler;

import java.util.Optional;
import org.connectorio.binding.base.config.Configuration;

public interface GenericNestedBridgeHandler<B extends GenericBridgeHandler, C extends Configuration> extends GenericBridgeHandler<C> {

  Optional<B> getBridgeHandler();

}
