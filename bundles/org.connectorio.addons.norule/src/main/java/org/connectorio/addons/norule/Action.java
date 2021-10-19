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
package org.connectorio.addons.norule;

/**
 * Wrapper for single instance of method defined in anonymous/annotated thing actions.
 *
 * This type allows to call the action without knowing its exact type.
 * Since openHAB 3 some actions are not exposed in type safe way (they can't be cast to any interface type) this type is
 * a high level wrapper.
 *
 * @param <T> Type of result
 */
public interface Action<T> {

  Action<T> setInput(String name, Object value);

  T invoke(String action);

}
