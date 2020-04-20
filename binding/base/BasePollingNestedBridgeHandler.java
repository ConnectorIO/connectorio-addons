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
package org.connectorio.binding.base.handler.polling.common;

import java.util.Optional;
import org.connectorio.binding.base.GenericTypeUtil;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.GenericBridgeHandlerBase;
import org.connectorio.binding.base.handler.polling.PollingBridgeHandler;
import org.connectorio.binding.base.handler.polling.PollingNestedBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;

public abstract class BasePollingNestedBridgeHandler<B extends PollingBridgeHandler, C extends PollingConfiguration> extends
  GenericBridgeHandlerBase<C> implements PollingNestedBridgeHandler<B, C> {

  private final Class<B> bridgeType;

  public BasePollingNestedBridgeHandler(Bridge bridge) {
    super(bridge);
    this.bridgeType = GenericTypeUtil.<B>resolveTypeVariable("B", getClass())
      .orElseThrow(() -> new IllegalArgumentException("Could not resolve bridge type"));
  }

  @Override
  public Optional<B> getBridgeHandler() {
    return Optional.of(getBridge())
      .map(Bridge::getHandler)
      .filter(bridgeType::isInstance)
      .map(handler -> (B) handler);
  }

  public Long getRefreshInterval() {
    return getBridgeConfig().map(cfg -> cfg.refreshInterval)
      .orElseGet(this::getDefaultPollingInterval);
  }

  protected abstract Long getDefaultPollingInterval();

}
