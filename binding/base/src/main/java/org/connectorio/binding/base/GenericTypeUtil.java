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
