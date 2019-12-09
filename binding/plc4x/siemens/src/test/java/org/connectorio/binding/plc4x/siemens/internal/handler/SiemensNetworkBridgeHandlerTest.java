package org.connectorio.binding.plc4x.siemens.internal.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.s7.connection.S7PlcConnection;
import org.connectorio.binding.plc4x.siemens.internal.config.SiemensNetworkConfiguration;
import org.connectorio.binding.test.BridgeMock;
import org.eclipse.smarthome.core.thing.Bridge;
import org.junit.jupiter.api.Test;

class SiemensNetworkBridgeHandlerTest {

  @Test
  void testHandlerInitializationWithNoConfig() {
    Bridge bridge = new BridgeMock<>()
      .withConfig(new SiemensNetworkConfiguration())
      .create();

    SiemensNetworkBridgeHandler handler = new SiemensNetworkBridgeHandler(bridge);
    handler.initialize();

    CompletableFuture<S7PlcConnection> initializer = handler.getInitializer();
    assertThatThrownBy(initializer::join).isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(PlcConnectionException.class)
      .hasMessageContaining("Error parsing address");
  }

  @Test
  void testHandlerInitializationWithConfig() {
    SiemensNetworkConfiguration cfg = new SiemensNetworkConfiguration();
    cfg.host = "127.0.0.1";
    cfg.rack = 0;
    cfg.slot = 0;

    Bridge bridge = new BridgeMock<>()
      .withConfig(cfg)
      .create();

    SiemensNetworkBridgeHandler handler = new SiemensNetworkBridgeHandler(bridge);
    handler.initialize();

    CompletableFuture<S7PlcConnection> initializer = handler.getInitializer();
    assertThatThrownBy(initializer::join).isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(PlcConnectionException.class)
      .hasMessageContaining("Error creating channel");
  }

}