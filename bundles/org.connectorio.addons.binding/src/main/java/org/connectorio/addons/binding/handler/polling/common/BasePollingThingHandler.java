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
package org.connectorio.addons.binding.handler.polling.common;

import org.connectorio.addons.binding.handler.GenericThingHandlerBase;
import org.connectorio.addons.binding.handler.PollingHandler;
import org.connectorio.addons.binding.handler.polling.PollingBridgeHandler;
import org.connectorio.addons.binding.config.PollingConfiguration;
import org.connectorio.addons.binding.handler.polling.PollingThingHandler;
import org.openhab.core.thing.Thing;

public abstract class BasePollingThingHandler<B extends PollingBridgeHandler<?>, C extends PollingConfiguration> extends
  GenericThingHandlerBase<B, C> implements PollingThingHandler<B, C> {

  public BasePollingThingHandler(Thing thing) {
    super(thing);
  }

  public Long getRefreshInterval() {
    return getThingConfig().map(cfg -> cfg.refreshInterval)
      .filter(interval -> interval > 0)
      .orElseGet(() -> getBridgeHandler().map(PollingHandler::getRefreshInterval)
        .orElseGet(this::getDefaultPollingInterval)
      );
  }

  protected abstract Long getDefaultPollingInterval();

}
