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
package org.connectorio.addons.binding.handler;

import java.util.Optional;
import org.connectorio.addons.binding.GenericTypeUtil;
import org.connectorio.addons.binding.config.PollingConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;

public abstract class GenericThingHandlerBase<B extends BridgeHandler, C extends PollingConfiguration> extends
    BaseThingHandler implements GenericThingHandler<B, C> {

  private final Class<B> bridgeType;
  private final Class<C> configType;

  public GenericThingHandlerBase(Thing thing) {
    super(thing);
    this.bridgeType = GenericTypeUtil.<B>resolveTypeVariable("B", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve bridge type"));
    this.configType = GenericTypeUtil.<C>resolveTypeVariable("C", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve config type"));
  }

  @Override
  public Optional<C> getThingConfig() {
    return Optional.ofNullable(super.getConfigAs(configType));
  }

  @Override
  public Optional<B> getBridgeHandler() {
    return Optional.ofNullable(getBridge())
      .map(Bridge::getHandler)
      .filter(bridgeType::isInstance)
      .map(handler -> (B) handler);
  }

//  protected final Class<B> getBridgeType() {
//    return bridgeType;
//  }

}
