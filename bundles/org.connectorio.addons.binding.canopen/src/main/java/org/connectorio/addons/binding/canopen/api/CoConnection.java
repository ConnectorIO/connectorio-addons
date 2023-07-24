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
package org.connectorio.addons.binding.canopen.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.canopen.readwrite.CANOpenService;
import org.apache.plc4x.java.spi.values.PlcStruct;

public interface CoConnection {

  CoNode getNode(int nodeId);

  CompletableFuture<CoSubscription> subscribe(int nodeId, CANOpenService service, Consumer<byte[]> consumer);

  CompletableFuture<CoSubscription> heartbeat(int nodeId, Consumer<PlcStruct> consumer);

  void close();

  void send(int nodeId, CANOpenService service, PlcValue value);

  // local node
  CoNode getLocalNode();

}
