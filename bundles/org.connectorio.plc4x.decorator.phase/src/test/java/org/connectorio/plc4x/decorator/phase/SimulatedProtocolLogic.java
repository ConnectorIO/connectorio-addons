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
package org.connectorio.plc4x.decorator.phase;

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.simulated.connection.SimulatedConnection;
import org.apache.plc4x.java.spi.ConversationContext;
import org.apache.plc4x.java.spi.Plc4xProtocolBase;

public class SimulatedProtocolLogic<T> extends Plc4xProtocolBase<T> {

  public final SimulatedConnection connection;

  public SimulatedProtocolLogic(SimulatedConnection connection) {
    this.connection = connection;
  }

  @Override
  public CompletableFuture<PlcReadResponse> read(PlcReadRequest readRequest) {
    return connection.read(readRequest);
  }

  @Override
  public CompletableFuture<PlcWriteResponse> write(PlcWriteRequest writeRequest) {
    return connection.write(writeRequest);
  }

  @Override
  public CompletableFuture<PlcSubscriptionResponse> subscribe(PlcSubscriptionRequest subscriptionRequest) {
    return connection.subscribe(subscriptionRequest);
  }

  @Override
  public CompletableFuture<PlcUnsubscriptionResponse> unsubscribe(PlcUnsubscriptionRequest unsubscriptionRequest) {
    return connection.unsubscribe(unsubscriptionRequest);
  }

  @Override
  public void close(ConversationContext<T> context) {

  }

}
