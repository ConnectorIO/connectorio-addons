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
package org.connectorio.addons.binding.fatek.internal.transport.command;

import static org.connectorio.addons.binding.fatek.internal.transport.ReflectiveCall.call;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import org.simplify4u.jfatek.FatekCommand;
import org.simplify4u.jfatek.FatekException;
import org.simplify4u.jfatek.FatekNotSentException;
import org.simplify4u.jfatek.FatekPLC;
import org.simplify4u.jfatek.io.FatekConnection;
import org.simplify4u.jfatek.io.FatekIOException;
import org.simplify4u.jfatek.io.FatekWriter;

public class MultiplexCmd<T> extends FatekCommand<T> {

  private final int stationNumber;
  private final FatekCommand<T> delegate;

  public MultiplexCmd(FatekPLC fatekPLC, int stationNumber, FatekCommand<T> delegate) {
    super(fatekPLC);
    this.stationNumber = stationNumber;
    this.delegate = delegate;
  }

  @Override
  public int getID() {
    return delegate.getID();
  }

  @Override
  protected void execute(FatekConnection conn) throws FatekIOException, FatekException {
    FatekConnection multiplexer = new FatekConnection(null) {
      @Override
      protected InputStream getInputStream() throws IOException {
        return io(() -> call(FatekConnection.class, conn, "getInputStream"));
      }

      @Override
      protected OutputStream getOutputStream() throws IOException {
        return io(() -> call(FatekConnection.class, conn, "getOutputStream"));
      }

      @Override
      protected void closeConnection() throws IOException {
        io(() -> call(FatekConnection.class, conn, "closeConnection"));
      }

      @Override
      public boolean isConnected() {
        try {
          return call(FatekConnection.class, conn, "isConnected");
        } catch (Throwable e) {
          return false;
        }
      }

      @Override
      public int getPlcId() {
        return stationNumber;
      }
    };

    fatekIo(() -> call(FatekCommand.class,  delegate, "execute", FatekConnection.class, multiplexer));
    fatekIo(() -> call(FatekCommand.class, delegate, "setAlreadySent", boolean.class, true));
  }

  private <X> X io(FailAbleCall<X> callable) throws IOException {
    try {
      return callable.call();
    } catch (Throwable e) {
      throw new IOException(e);
    }
  }

  private <X> X fatekIo(FailAbleCall<X> callable) throws FatekIOException {
    try {
      return callable.call();
    } catch (Throwable e) {
      throw new FatekIOException(e);
    }
  }

  @Override
  protected void writeData(FatekWriter fatekWriter) throws FatekException, FatekIOException {
    throw new FatekException("This method should never be called!");
  }

  @Override
  public T getResult() throws FatekNotSentException {
    try {
      return call(FatekCommand.class, delegate, "getResult");
    } catch (Throwable e) {
      FatekNotSentException notSentException = new FatekNotSentException();
      notSentException.initCause(e);
      throw notSentException;
    }
  }

  interface FailAbleCall<X> {
    X call() throws Throwable;
  }

}
