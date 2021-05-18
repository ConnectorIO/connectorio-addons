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
package org.connectorio.plc4x.decorator.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadRequest.Builder;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.simulated.connection.SimulatedConnection;
import org.apache.plc4x.java.simulated.connection.SimulatedDevice;
import org.connectorio.plc4x.decorator.DecoratorConnection;
import org.junit.jupiter.api.Test;

class RetryDecoratorReadTest {

  public static final String TEST_FIELD_NAME = "test";
  private int failureLimit = 0;

  @Test
  void check() throws Exception {
    SimulatedDevice device = new SimulatedDevice("fo");
    AtomicInteger attempts = new AtomicInteger(0);
    SimulatedConnection connection = new SimulatedConnection(device) {
      @Override
      public CompletableFuture<PlcReadResponse> read(PlcReadRequest readRequest) {
        if (attempts.getAndIncrement() < failureLimit) {
          CompletableFuture<PlcReadResponse> future = new CompletableFuture<>();
          future.completeExceptionally(new PlcRuntimeException("Runtime error"));
          return future;
        }
        return super.read(readRequest);
      }
    };

    DecoratorConnection delegate = new DecoratorConnection(connection, new RetryDecorator(2), null, null, null);
    connection.setProtocol(new SimulatedProtocolLogic<>(connection));
    delegate.connect();

    write(delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME,"STATE/test:INTEGER", 10));
    failureLimit = 2;
    PlcReadResponse response = read(createRequest(delegate));
    assertThat(response.getInteger(TEST_FIELD_NAME)).isEqualTo(10);

    attempts.set(0);
    failureLimit = 2;
    write(delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME,"STATE/test:INTEGER", 20));
    response = read(createRequest(delegate));
    assertThat(response.getInteger(TEST_FIELD_NAME)).isEqualTo(20);
    assertThat(attempts.get()).isEqualTo(3);

    attempts.set(0);
    failureLimit = 3;
    assertThatThrownBy(() -> read(createRequest(delegate)))
      .isInstanceOf(ExecutionException.class);
    assertThat(attempts.get()).isEqualTo(3);
  }

  private Builder createRequest(DecoratorConnection delegate) {
    return delegate.readRequestBuilder().addItem(TEST_FIELD_NAME, "STATE/test:INTEGER");
  }

  private PlcReadResponse read(Builder readBuilder) throws Exception {
    return readBuilder.build().execute().get();
  }

  private PlcWriteResponse write(PlcWriteRequest.Builder writeBuilder) throws Exception {
    return writeBuilder.build().execute().get();
  }

}