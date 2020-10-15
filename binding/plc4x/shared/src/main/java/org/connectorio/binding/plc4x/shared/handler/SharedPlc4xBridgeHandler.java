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
package org.connectorio.binding.plc4x.shared.handler;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.binding.base.config.PollingConfiguration;
import org.connectorio.binding.base.handler.polling.common.BasePollingBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SharedPlc4xBridgeHandler<T extends PlcConnection, C extends PollingConfiguration> extends
    BasePollingBridgeHandler<C> implements BridgeHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private T connection;

  public SharedPlc4xBridgeHandler(Bridge bridge) {
    super(bridge);
  }

  public T getConnection() {
    // make sure we create new connection only if there is none.
    if (connection == null) {
      logger.debug("Opening new connection to device");
      connection = getPlcConnection();
    }
    return connection;
  }

  protected abstract T getPlcConnection();

  public abstract CompletableFuture<T> getInitializer();

  @Override
  public void dispose() {
    if (connection != null) {
      try {
        if (connection.isConnected()) {
          connection.close();
          logger.debug("Closed connection to device");
        }
      } catch (Exception e) {
        logger.warn("Error while terminating connection to device", e);
      }
      connection = null;
    }
  }

  protected String hostWithPort(String host, Integer port) {
    return host + (port == null ? "" : ":" + port);
  }

}
