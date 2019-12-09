package org.connectorio.binding.base.handler.polling;

import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.GenericNestedBridgeHandler;
import org.connectorio.binding.base.handler.PollingHandler;

public interface PollingNestedBridgeHandler<B extends PollingBridgeHandler, C extends PollingConfiguration> extends GenericNestedBridgeHandler<B, C>, PollingHandler {

}
