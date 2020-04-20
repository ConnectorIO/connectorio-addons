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
package org.connectorio.binding.base;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import java.lang.reflect.Type;
import java.util.Optional;

public class GenericTypeUtil {

  public static <X> Optional<Class<X>> resolveTypeVariable(String variable, Type type) {
    TypeResolver resolver = new TypeResolver();
    return resolveTypeVariable(resolver, variable, type);
  }

  public static <X> Optional<Class<X>> resolveTypeVariable(TypeResolver resolver, String variable, Type type) {
    if (type == null) {
      return Optional.empty();
    }

    // a kind of brute force attempt to resolve type variables using classmate
    ResolvedType resolvedType = resolver.resolve(type);
    ResolvedType boundType = resolvedType.getTypeBindings().findBoundType(variable);

    if (boundType != null) {
      return Optional.of((Class<X>) boundType.getErasedType());
    }

    // we failed to resolve interesting variable so we have to dig more
    if (type instanceof Class) {
      Class<?> clazz = (Class<?>) type;

      Type[] interfaces = clazz.getGenericInterfaces();
      for (Type iface : interfaces) {
        Optional<Class<X>> bound = resolveTypeVariable(variable, iface);
        if (bound.isPresent()) {
          return bound;
        }
      }

      Type superClazz = clazz.getGenericSuperclass();
      return resolveTypeVariable(variable, superClazz);
    }

    return Optional.empty();
  }

}
