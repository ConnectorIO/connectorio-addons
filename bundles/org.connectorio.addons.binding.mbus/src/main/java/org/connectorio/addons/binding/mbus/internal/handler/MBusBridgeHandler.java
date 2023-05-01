/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.mbus.internal.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.connectorio.addons.binding.handler.polling.PollingBridgeHandler;
import org.connectorio.addons.binding.mbus.config.BridgeConfig;
import org.connectorio.addons.binding.mbus.internal.discovery.DiscoveryCoordinator;
import org.openmuc.jmbus.MBusConnection;

public interface MBusBridgeHandler<C extends BridgeConfig> extends PollingBridgeHandler<C> {

  CompletableFuture<MBusConnection> getConnection();

  CompletionStage<DiscoveryCoordinator> getDiscoveryCoordinator();

}
