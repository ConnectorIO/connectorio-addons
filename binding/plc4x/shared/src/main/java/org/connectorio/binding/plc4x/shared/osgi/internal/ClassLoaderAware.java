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
 */
package org.connectorio.binding.plc4x.shared.osgi.internal;

import java.util.Arrays;
import java.util.concurrent.Callable;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;

/**
 * An additional helper type to propagate additional class loader to caller thread context.
 */
public class ClassLoaderAware {

  public static <T> T call(ClassLoader loader, Callable<T> callable) throws PlcRuntimeException, PlcConnectionException {
    ClassLoader context = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(new CompoundClassLoader(Arrays.asList(loader, context)));
      return callable.call();
    } catch (Exception e) {
      if (e instanceof PlcConnectionException) {
        throw (PlcConnectionException) e;
      }
      if (e instanceof PlcRuntimeException) {
        throw (PlcRuntimeException) e;
      }
      throw new PlcRuntimeException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(context);
    }
  }

}
