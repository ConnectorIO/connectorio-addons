/*
 * Copyright (C) 2019-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.test;

import static org.mockito.Mockito.when;

import org.connectorio.addons.binding.config.Configuration;
import org.connectorio.addons.binding.handler.GenericThingHandler;
import org.mockito.Mockito;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;

public class ThingMock<T extends GenericThingHandler, C extends Configuration> {

  private final Thing thing;
  private final ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
  private final ConfigurationMock<C> config = new ConfigurationMock<>();
  private T handler;

  public ThingMock() {
    this("Thing " + Math.random());
  }

  public ThingMock(String name) {
    thing = Mockito.mock(Bridge.class, Mockito.withSettings().name(name));
  }

  public ThingMock<T, C> withId(String id) {
    return withId(new ThingUID(id));
  }

  public ThingMock<T, C> withId(ThingUID id) {
    when(thing.getUID()).thenReturn(id);
    return this;
  }

  public ThingMock<T, C> withConfig(C mapped) {
    org.openhab.core.config.core.Configuration cfg = config.get(mapped);
    when(thing.getConfiguration()).thenReturn(cfg);
    return this;
  }

  public ThingMock<T, C> withBridge(Bridge parent) {
    ThingUID parentUid = parent.getUID();
    when(thing.getBridgeUID()).thenReturn(parentUid);
    when(callback.getBridge(parentUid)).thenReturn(parent);
    return this;
  }

  public ThingMock<T, C> withHandler(T target) {
    this.handler = target;
    when(thing.getHandler()).thenReturn(this.handler);
    return this;
  }

  public ThingHandlerCallback getCallback() {
    return callback;
  }

  public Thing create() {
    return thing;
  }

  public ThingHandler getHandler() {
    return handler;
  }

}
