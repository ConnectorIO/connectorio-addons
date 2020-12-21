/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.binding.plc4x.siemens.internal.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.s7.readwrite.S7Driver;
import org.assertj.core.api.AbstractThrowableAssert;
import org.connectorio.addons.binding.plc4x.osgi.internal.OsgiDriverManager;
import org.connectorio.addons.binding.plc4x.siemens.internal.config.SiemensNetworkConfiguration;
import org.connectorio.addons.binding.test.BridgeMock;
import org.eclipse.smarthome.core.thing.Bridge;
import org.junit.jupiter.api.Test;

class SiemensNetworkBridgeHandlerTest {

  @Test
  void testHandlerInitializationWithNoConfig() {
    Bridge bridge = new BridgeMock<>()
      .withConfig(new SiemensNetworkConfiguration())
      .create();

    SiemensNetworkBridgeHandler handler = new SiemensNetworkBridgeHandler(bridge, new OsgiDriverManager(Collections.emptyList()));
    handler.initialize();

    CompletableFuture<PlcConnection> initializer = handler.getPlcConnection();
    AbstractThrowableAssert<?, ? extends Throwable> thrownBy = assertThatThrownBy(initializer::join);
    thrownBy.isInstanceOf(CompletionException.class)
      .hasMessageContaining("Error creating channel.");

    thrownBy = thrownBy.getCause();
    thrownBy.isInstanceOf(PlcConnectionException.class)
      .hasMessageContaining("Error creating channel.");

    thrownBy = thrownBy.getCause();
    thrownBy.isInstanceOf(UnknownHostException.class)
      .hasMessageContaining("null");
  }

  @Test
  void testHandlerInitializationWithConfig() {
    SiemensNetworkConfiguration cfg = new SiemensNetworkConfiguration();
    cfg.host = "127.0.0.1";
    cfg.localRack = 10;
    cfg.localSlot = 11;

    Bridge bridge = new BridgeMock<>()
      .withConfig(cfg)
      .create();

    SiemensNetworkBridgeHandler handler = new SiemensNetworkBridgeHandler(bridge, new OsgiDriverManager(Collections.emptyList()));
    handler.initialize();

//    CompletableFuture<AbstractPlcConnection> initializer = handler.getInitializer();
//    assertThatThrownBy(initializer::join).isInstanceOf(CompletionException.class)
//      .hasCauseInstanceOf(PlcConnectionException.class)
//      .hasMessageContaining("Unable to Connect on TCP Layer");

    CompletableFuture<PlcConnection> initializer = handler.getPlcConnection();

    AbstractThrowableAssert<?, ? extends Throwable> thrownBy = assertThatThrownBy(initializer::join);
    thrownBy.isInstanceOf(CompletionException.class)
      .hasMessageContaining("Error creating channel.");

    thrownBy = thrownBy.getCause();
    thrownBy.isInstanceOf(PlcConnectionException.class);

    thrownBy = thrownBy.getCause();
    thrownBy.isNotExactlyInstanceOf(ConnectException.class)
      .hasMessageContaining("Connection refused: /%s:%d", cfg.host, S7Driver.ISO_ON_TCP_PORT);
  }

}
