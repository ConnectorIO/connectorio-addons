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
package org.connectorio.plc4x.decorator.phase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.simulated.connection.SimulatedConnection;
import org.apache.plc4x.java.simulated.connection.SimulatedDevice;
import org.connectorio.plc4x.decorator.DecoratorConnection;
import org.junit.jupiter.api.Test;

class PhaseDecoratorErrorTest {

  public static final String TEST_FIELD_NAME = "test";

  @Test
  void check() throws Exception {
    SimulatedDevice device = new SimulatedDevice("fo");
    SimulatedConnection connection = new SimulatedConnection(device) {
      AtomicInteger attempts = new AtomicInteger();
      @Override
      public CompletableFuture<PlcWriteResponse> write(PlcWriteRequest writeRequest) {
        if (attempts.getAndIncrement() > 2) {
          CompletableFuture<PlcWriteResponse> future = new CompletableFuture<>();
          future.completeExceptionally(new PlcRuntimeException("Runtime error"));
          return future;
        }
        return super.write(writeRequest);
      }
    };

    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch latch = new CountDownLatch(3);

    write(connection, "Device 1", start, latch);
    write(connection, "Device 2", start, latch);
    write(connection, "Device 3", start, latch);
    write(connection, "Device 4", start, latch);

    start.countDown();
    latch.await();
  }

  private void write(SimulatedConnection delegate, String phaseName, CountDownLatch start, CountDownLatch latch) throws InterruptedException, ExecutionException {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          start.await();
          Phase phase = Phase.create(phaseName, 150);
          phase.addCallback(latch::countDown);
          phase.addCallback(() -> System.out.println("Phase " + phaseName + " closed"));
          execute(delegate, phase);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void execute(SimulatedConnection connection, Phase phase) throws ExecutionException, InterruptedException, PlcConnectionException {
    PhaseDecorator decorator = new PhaseDecorator();
    DecoratorConnection delegate = new DecoratorConnection(connection, decorator, decorator, null, null);
    connection.setProtocol(new SimulatedProtocolLogic<>(connection));
    delegate.connect();

    delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME, "STATE/test:INTEGER", 10).build().execute().whenComplete((r1, e1) -> {
      System.out.println("Read A " + phase);
      delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME, "STATE/test:INTEGER", 20).build().execute().whenComplete((r2, e2) -> {
        System.out.println("Read B " + phase);
        delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME, "STATE/test:INTEGER", 30).build().execute().whenComplete((r3, e3) -> {
          System.out.println("Read C " + phase);
        });
      });
    }).whenComplete((r, e) -> {
      System.out.println("Read complete " + phase);
    }).get();
  }

}