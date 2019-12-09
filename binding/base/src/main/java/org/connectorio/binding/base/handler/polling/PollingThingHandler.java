package org.connectorio.binding.base.handler.polling;

import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.GenericThingHandler;
import org.connectorio.binding.base.handler.PollingHandler;

public interface PollingThingHandler<B extends PollingBridgeHandler, C extends PollingConfiguration> extends
  GenericThingHandler<B, C>, PollingHandler {

}
