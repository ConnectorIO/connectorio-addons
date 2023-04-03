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
package org.connectorio.addons.binding.wmbus.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.config.Configuration;
import org.connectorio.addons.binding.handler.GenericBridgeHandler;
import org.connectorio.addons.binding.wmbus.dispatch.WMBusMessageDispatcher;
import org.connectorio.addons.binding.wmbus.internal.KeyStore;
import org.connectorio.addons.binding.wmbus.internal.discovery.DiscoveryCoordinator;

public interface WMBusBridgeHandler<T extends Configuration> extends GenericBridgeHandler<T> {

  CompletableFuture<WMBusMessageDispatcher> getDispatcher();

  CompletableFuture<KeyStore> getKeyStore();

  CompletableFuture<DiscoveryCoordinator> getDiscoveryCoordinator();
}
