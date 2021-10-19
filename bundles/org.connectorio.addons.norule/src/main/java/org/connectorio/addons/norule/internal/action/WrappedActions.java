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
package org.connectorio.addons.norule.internal.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import org.connectorio.addons.norule.Action;
import org.openhab.core.thing.binding.ThingActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrappedActions<T> implements Action<T> {

  private final Logger logger = LoggerFactory.getLogger(WrappedActions.class);
  private final ThingActions actions;
  private final ClassLoader classLoader;
  private final Map<String, Object> inputs = new HashMap<>();

  public WrappedActions(ThingActions actions, ClassLoader classLoader) {
    this.actions = actions;
    this.classLoader = classLoader;
  }

  @Override
  public Action<T> setInput(String name, Object value) {
    inputs.put(name, value);
    return this;
  }

  /**
   * Navigate over annotations without refering them directly via imports.
   * An ugly way to detach itself from openhab automation while still having a way to interact with it.
   *
   * @param action Action (method) name.
   * @return Result of method call.
   */
  @Override
  public T invoke(String action) {
    for (Method method : actions.getClass().getMethods()) {
      if (action.equals(method.getName()) && hasAnnotation(method, "org.openhab.core.automation.annotation.RuleAction") && method.getParameterCount() <= inputs.size()) {
        Annotation output = getAnnotation(method, "org.openhab.core.automation.annotation.ActionOutput");
        if (output == null) {
          logger.warn("Ignoring method {}, it has no @ActionOutput annotation", method.getName());
          continue;
        }
        String returnTypeName = getParameter(output, "type");
        try {
          Class<?> returnType = classLoader.loadClass(returnTypeName);
          boolean matched = true;
          Object[] params = new Object[method.getParameterCount()];
          Class<?>[] paramTypes = new Class[method.getParameterCount()];
          Parameter[] parameters = method.getParameters();
          for (int index = 0, parametersLength = parameters.length; index < parametersLength; index++) {
            Parameter parameter = parameters[index];
            Annotation input = getAnnotation(parameter, "org.openhab.core.automation.annotation.ActionInput");
            if (input == null) {
              continue;
            }
            String inputName = getParameter(input, "name");
            if (!inputs.containsKey(inputName)) {
              logger.debug("Action miss required input {}, ignoring.", inputName);
              matched = false;
              break;
            }
            Object inputValue = inputs.get(inputName);
            if (!parameter.getType().isInstance(inputValue)) {
              logger.debug("Action specified input {} of wrong type {} ignoring. Please set it to {}.", inputName, (inputValue == null ? "null" : inputValue.getClass()), parameter.getType());
              matched = false;
              break;
            }
            params[index] = inputValue;
          }
          if (matched) {
            return (T) invoike(method, actions, params);
          }
        } catch (ClassNotFoundException e) {
          logger.error("Could not resolve type {}", returnTypeName, e);
          return null;
        }

      }
    }
    logger.warn("Could not resolve action {} with arguments {}", action, inputs);
    return null;
  }

  private boolean hasAnnotation(AnnotatedElement element, String className) {
    return getAnnotation(element, className) != null;
  }

  private Annotation getAnnotation(AnnotatedElement element, String className) {
    for (Annotation annotation : element.getAnnotations()) {
      if (annotation.annotationType().getName().equals(className)) {
        return annotation;
      }
    }
    return null;
  }

  private <T> T getParameter(Annotation annotation, String param) {
    for (Method parameter : annotation.getClass().getMethods()) {
      if (param.equals(parameter.getName())) {
        return invoike(parameter, annotation);
      }
    }
    return null;
  }

  private <T> T invoike(Method method, Object instance, Object ... args) {
    try {
      return (T) method.invoke(instance, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      logger.error("Error while calling method {}", method, e);
      return null;
    }
  }
}
