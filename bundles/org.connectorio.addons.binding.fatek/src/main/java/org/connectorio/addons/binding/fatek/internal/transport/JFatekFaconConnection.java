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
package org.connectorio.addons.binding.fatek.internal.transport;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.connectorio.addons.binding.fatek.internal.transport.command.MultiplexCmd;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekException;
import org.simplify4u.jfatek.FatekPLC;
import org.simplify4u.jfatek.io.FatekIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JFatekFaconConnection implements FaconConnection {

  private final Logger logger = LoggerFactory.getLogger(JFatekFaconConnection.class);
  private final ExecutorService executor;
  private final FatekPLC connection;

  public JFatekFaconConnection(ExecutorService executor, FatekPLC connection) {
    this.executor = executor;
    this.connection = connection;
  }

  @Override
  public <T> CompletableFuture<T> execute(int stationNo, FatekCommand<T> command) {
    CompletableFuture<T> future = new CompletableFuture<>();
    executor.execute(() -> {
      try {
        synchronized (connection) {
          T result = new MultiplexCmd<>(connection, stationNo, command).send();
          future.complete(result);
        }
      } catch (FatekException | FatekIOException e) {
        future.completeExceptionally(e);
      } catch (Exception e) {
        logger.error("Command {} generated unexpected error", command, e);
      }
    });
    return future;
  }

  @Override
  public FatekPLC asFatek() {
    return connection;
  }

  @Override
  public void close() throws IOException {
    connection.close();
  }

  @Override
  public String toString() {
    return getClass().getName() + "[ " + connection + "]";
  }

}
