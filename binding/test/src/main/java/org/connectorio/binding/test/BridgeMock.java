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
package org.connectorio.binding.test;

import static org.mockito.Mockito.when;

import org.connectorio.binding.base.config.Configuration;
import org.connectorio.binding.base.handler.GenericBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.mockito.Mockito;

public class BridgeMock<B extends GenericBridgeHandler<C>, C extends Configuration> {

  private final Bridge bridge;
  private final ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
  private final ConfigurationMock<C> config = new ConfigurationMock<>();
  private BridgeHandler handler;

  public BridgeMock() {
    this("Bridge " + Math.random());
  }

  public BridgeMock(String name) {
    bridge = Mockito.mock(Bridge.class, Mockito.withSettings().name(name));
  }

  public BridgeMock<B, C> withId(String id) {
    return withId(new ThingUID(id));
  }

  public BridgeMock<B, C> withId(ThingUID id) {
    when(bridge.getUID()).thenReturn(id);
    return this;
  }

  public BridgeMock<B, C> withConfig(C mapped) {
    org.openhab.core.config.core.Configuration cfg = config.get(mapped);
    when(bridge.getConfiguration()).thenReturn(cfg);
    return this;
  }

  public BridgeMock<B, C> withBridge(Bridge parent) {
    ThingUID parentUid = parent.getUID();
    when(bridge.getBridgeUID()).thenReturn(parentUid);
    when(callback.getBridge(parentUid)).thenReturn(parent);
    return this;
  }

  public <X extends BridgeHandler> BridgeMock<B, C> mockHandler(Class<X> type) {
    handler = Mockito.mock(type);
    when(bridge.getHandler()).thenReturn(handler);
    return this;
  }

  public BridgeMock<B, C> mockHandler() {
    return mockHandler(BridgeHandler.class);
  }

  public ThingHandlerCallback getCallback() {
    return callback;
  }

  public Bridge create() {
    return bridge;
  }

  public BridgeHandler getHandler() {
    return handler;
  }

}
