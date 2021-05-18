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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler.builder.linking;

import java.util.Objects;

public class ObjectKey {
  final Class<?> type;
  final int index;

  public ObjectKey(Class<?> type, int index) {
    this.type = type;
    this.index = index;
  }

  public Class<?> getType() {
    return type;
  }

  public int getIndex() {
    return index;
  }

  @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ObjectKey)) {
        return false;
      }
      ObjectKey key = (ObjectKey) o;
      return index == key.index && Objects.equals(type, key.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, index);
    }
  }