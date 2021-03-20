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
package org.connectorio.addons.binding.plc4x.canopen.handler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.plc4x.canopen.api.CoConnection;
import org.connectorio.addons.binding.plc4x.canopen.config.CoNodeConfig;
import org.connectorio.addons.binding.plc4x.canopen.discovery.CoDiscoveryParticipant;
import org.connectorio.addons.binding.plc4x.handler.Plc4xBridgeHandler;
import org.connectorio.plc4x.decorator.Decorator;

public interface CoBridgeHandler<C extends CoNodeConfig> extends Plc4xBridgeHandler<PlcConnection, C> {

  String getName();

  int getNodeId();

  List<CoDiscoveryParticipant> getParticipants();

  /**
   * Returns CANopen connection which is aware of some semantics coming from CANopen itself. This is distinct from an
   * PlcConnection available through {@link #getConnection()}.
   *
   * The {@link #getConnection()} method gives direct access to the bus and allows to submit lower level operations.
   * This method is intended to bring semantics which are a bit closer to CANopen and possibly object dictionary.
   *
   * @param decorators Additional decorator objects to apply on the connection.
   * @return CANopen connection once actual transport connection is established.
   */
  CompletableFuture<CoConnection> getCoConnection(Decorator... decorators);

}
