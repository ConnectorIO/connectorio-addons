package org.connectorio.binding.test;

import static org.mockito.Mockito.when;

import org.connectorio.binding.base.config.Configuration;
import org.connectorio.binding.base.handler.GenericBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.mockito.Mockito;

public class BridgeMock<B extends GenericBridgeHandler<C>, C extends Configuration> {

  private Bridge bridge = Mockito.mock(Bridge.class);
  private ConfigurationMock<C> config = new ConfigurationMock<>();

  public BridgeMock<B, C> withConfig(C mapped) {
    org.eclipse.smarthome.config.core.Configuration cfg = config.get(mapped);
    when(bridge.getConfiguration()).thenReturn(cfg);
    return this;
  }

  public Bridge create() {
    return bridge;
  }

}
