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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import org.apache.plc4x.java.simulated.connection.SimulatedConnection;
import org.apache.plc4x.java.simulated.connection.SimulatedDevice;
import org.connectorio.plc4x.decorator.DecoratorConnection;
import org.junit.jupiter.api.Test;

class PhaseDecoratorTest {

  public static final String TEST_FIELD_NAME = "test";
  private int failureLimit = 0;

  @Test
  void check() throws Exception {
    SimulatedDevice device = new SimulatedDevice("fo");
    SimulatedConnection connection = new SimulatedConnection(device);

    PhaseDecorator decorator = new PhaseDecorator();
    DecoratorConnection delegate = new DecoratorConnection(connection, decorator, decorator, null, null);
    connection.setProtocol(new SimulatedProtocolLogic<>(connection));
    delegate.connect();

    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch latch = new CountDownLatch(3);

    write("Device 1", start, latch, delegate);
    write("Device 2", start, latch, delegate);
    write("Device 3", start, latch, delegate);
    write("Device 4", start, latch, delegate);

    start.countDown();
    latch.await();
    Thread.sleep(5001);
  }

  private void write(String phaseName, CountDownLatch start, CountDownLatch latch, DecoratorConnection delegate) throws InterruptedException, java.util.concurrent.ExecutionException {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          start.await();
          Phase.create(phaseName);
          execute(delegate);
          latch.countDown();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void execute(DecoratorConnection delegate) throws ExecutionException, InterruptedException {
    delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME, "STATE/test:INTEGER", 10).build().execute().whenComplete((r1, e1) -> {
      System.out.println(Phase.get());
      delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME, "STATE/test:INTEGER", 20).build().execute().whenComplete((r2, e2) -> {
        System.out.println(Phase.get());
        delegate.writeRequestBuilder().addItem(TEST_FIELD_NAME, "STATE/test:INTEGER", 30).build().execute().whenComplete((r3, e3) -> {
          System.out.println(Phase.get());
        });
      });
    }).whenComplete((r, e) -> {
      System.out.println(Phase.get());
    }).get();
  }

}