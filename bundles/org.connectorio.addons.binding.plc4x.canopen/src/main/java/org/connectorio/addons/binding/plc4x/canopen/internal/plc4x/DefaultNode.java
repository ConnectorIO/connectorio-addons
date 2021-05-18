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
package org.connectorio.addons.binding.plc4x.canopen.internal.plc4x;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.connectorio.addons.binding.plc4x.canopen.api.CoConnection;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.api.CoSubscription;

public class DefaultNode extends DefaultSdoAware implements CoNode {

  public DefaultNode(DefaultConnection connection, int nodeId) {
    super(connection, nodeId);
  }

  @Override
  public CompletableFuture<CoSubscription> subscribe(CANOpenService service, Consumer<byte[]> consumer) {
    return connection.subscribe(nodeId, service, consumer);
  }

  @Override
  public CoConnection getConnection() {
    return connection;
  }

  @Override
  public void close() {
    connection.remove(this);
  }

  @Override
  public String toString() {
    return "CoNode[" + nodeId + "]";
  }
}
