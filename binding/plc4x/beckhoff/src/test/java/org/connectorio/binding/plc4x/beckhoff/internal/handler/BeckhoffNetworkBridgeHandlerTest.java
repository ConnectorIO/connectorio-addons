package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.plc4x.java.ads.api.generic.types.AmsNetId;
import org.apache.plc4x.java.ads.connection.AdsTcpPlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffNetworkConfiguration;
import org.connectorio.binding.test.BridgeMock;
import org.eclipse.smarthome.core.thing.Bridge;
import org.junit.jupiter.api.Test;

class BeckhoffNetworkBridgeHandlerTest {

  @Test
  void testHandlerInitializationWithNoConfig() {
    Bridge bridge = new BridgeMock<>()
      .withConfig(new BeckhoffNetworkConfiguration())
      .create();

    BeckhoffNetworkBridgeHandler handler = new BeckhoffNetworkBridgeHandler(bridge);
    handler.initialize();

    CompletableFuture<AdsTcpPlcConnection> initializer = handler.getInitializer();
    assertThatThrownBy(initializer::join).isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(PlcConnectionException.class)
      .hasMessageContaining("doesn't match");
  }

  @Test
  void testHandlerInitializationWithConfig() {
    BeckhoffNetworkConfiguration cfg = new BeckhoffNetworkConfiguration();
    cfg.host = "127.0.0.1";
    cfg.port = 4040;
    cfg.targetAmsId = "0.0.0.0.0.0";
    cfg.targetAmsPort = 4040;
    cfg.sourceAmsId = "0.0.0.0.0.0";
    cfg.sourceAmsPort = 4040;

    Bridge bridge = new BridgeMock<>()
      .withConfig(cfg)
      .create();

    BeckhoffNetworkBridgeHandler handler = new BeckhoffNetworkBridgeHandler(bridge);
    handler.initialize();

    CompletableFuture<AdsTcpPlcConnection> initializer = handler.getInitializer();
    assertThatThrownBy(initializer::join).isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(PlcConnectionException.class)
      .hasMessageContaining("Error creating channel");
  }

}