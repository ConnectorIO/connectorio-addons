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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectiveCall {

  public static <X> X call(Class<?> type, Object ctx, String method) throws Throwable {
    try {
      Method mth = type.getDeclaredMethod(method);
      mth.setAccessible(true);
      return (X) mth.invoke(ctx);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static  <X> X call(Class<?> type, Object ctx, String method, Class<?> arg, Object val) throws Throwable {
    try {
      Method mth = type.getDeclaredMethod(method, arg);
      mth.setAccessible(true);
      return (X) mth.invoke(ctx, val);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static  <X> X field(Class<?> type, Object ctx, String fieldName) {
    try {
      Field fld = type.getDeclaredField(fieldName);
      fld.setAccessible(true);
      return (X) fld.get(ctx);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
