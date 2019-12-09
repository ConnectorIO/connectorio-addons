package org.connectorio.binding.base.handler.polling;

import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.GenericBridgeHandler;
import org.connectorio.binding.base.handler.PollingHandler;

public interface PollingBridgeHandler<C extends PollingConfiguration> extends GenericBridgeHandler<C>, PollingHandler {

}
