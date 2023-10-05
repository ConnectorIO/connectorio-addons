/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.amsads.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.assertj.core.api.AbstractThrowableAssert;
import org.connectorio.addons.binding.amsads.internal.handler.channel.ChannelHandlerFactory;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReader;
import org.connectorio.addons.binding.amsads.internal.symbol.SymbolReaderFactory;
import org.connectorio.addons.binding.config.Configuration;
import org.connectorio.addons.binding.amsads.AmsAdsBindingConstants;
import org.connectorio.addons.binding.amsads.internal.config.AmsConfiguration;
import org.connectorio.addons.binding.amsads.internal.config.NetworkConfiguration;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsDiscoveryDriver;
import org.connectorio.addons.binding.test.ThingMock;
import org.connectorio.plc4x.extras.osgi.core.internal.OsgiDriverManager;
import org.connectorio.addons.binding.test.BridgeMock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.binding.ThingHandler;

@ExtendWith(MockitoExtension.class)
class AmsAdsNetworkBridgeHandlerTest {

  @Mock
  SymbolReaderFactory symbolReaderFactory;
  @Mock
  SymbolReader symbolReader;
  @Mock
  ChannelHandlerFactory channelHandlerFactory;

  @Mock
  AmsAdsDiscoveryDriver discoveryDriver;

  @Test
  void testHandlerInitializationWithNoConfig() {
    Bridge bridge = new BridgeMock<>()
      .create();

    AmsAdsNetworkBridgeHandler handler = new AmsAdsNetworkBridgeHandler(bridge, symbolReaderFactory,
      channelHandlerFactory, new OsgiDriverManager(Collections.emptyList()), discoveryDriver);
    handler.initialize();

    CompletableFuture<PlcConnection> initializer = handler.getPlcConnection();
    assertThat(initializer).isNotCompleted();
  }

  @Test
  void testHandlerInitializationWithConfig() {
    NetworkConfiguration cfg = new NetworkConfiguration();
    cfg.host = "0.0.0.0";
    cfg.targetAmsId = "192.168.1.1.1.1";
    cfg.targetAmsPort = 4040;

    AmsConfiguration amsCfg = new AmsConfiguration();
    amsCfg.sourceAmsId = "127.0.0.0.1.1";
    amsCfg.sourceAmsPort = 4040;
    amsCfg.ipAddress = "0.0.0.1";

    BridgeMock<AmsBridgeHandler, AmsConfiguration> amsBridgeMock = new BridgeMock<AmsBridgeHandler, AmsConfiguration>("ams-network")
      .withId(new ThingUID(AmsAdsBindingConstants.THING_TYPE_AMS, "amsads-1"))
      .mockHandler(AmsBridgeHandler.class)
      .withConfig(amsCfg);
    Bridge amsBridge = amsBridgeMock.create();

    ThingMock<AmsAdsNetworkBridgeHandler, Configuration> thingMock = new ThingMock<AmsAdsNetworkBridgeHandler, Configuration>("ads-connection")
      .withConfig(cfg)
      .withBridge(amsBridge);
    Thing thing = thingMock.create();

    AmsAdsNetworkBridgeHandler handler = new AmsAdsNetworkBridgeHandler(thing, symbolReaderFactory,
      channelHandlerFactory, new OsgiDriverManager(Arrays.asList(getClass().getClassLoader())), discoveryDriver);
    handler.setCallback(thingMock.getCallback());
    handler.initialize();

    CompletableFuture<PlcConnection> initializer = handler.getPlcConnection();

    AbstractThrowableAssert<?, ? extends Throwable> thrownBy = assertThatThrownBy(initializer::join);
    assertThat(initializer).isCompletedExceptionally();

    thrownBy.isInstanceOf(CompletionException.class)
      .hasMessageContaining("Error creating channel.");

    thrownBy = thrownBy.getCause();
    thrownBy.isInstanceOf(PlcConnectionException.class);
  }

}