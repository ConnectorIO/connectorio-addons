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
package org.connectorio.addons.binding.handler;

import java.util.Optional;
import org.connectorio.addons.binding.GenericTypeUtil;
import org.connectorio.addons.binding.config.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BaseBridgeHandler;

public abstract class GenericBridgeHandlerBase<C extends Configuration> extends
    BaseBridgeHandler implements GenericBridgeHandler<C> {

  private final Class<C> configType;

  public GenericBridgeHandlerBase(Bridge bridge) {
    super(bridge);
    this.configType = GenericTypeUtil.<C>resolveTypeVariable("C", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve config type"));
  }

  @Override
  public Optional<C> getBridgeConfig() {
    return Optional.ofNullable(super.getConfigAs(configType));
  }

}
