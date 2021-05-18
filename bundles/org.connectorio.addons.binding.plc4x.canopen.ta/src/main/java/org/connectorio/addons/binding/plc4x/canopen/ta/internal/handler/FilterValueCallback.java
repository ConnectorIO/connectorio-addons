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
package org.connectorio.addons.binding.plc4x.canopen.ta.internal.handler;

import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.dev.ValueCallback;
import org.connectorio.addons.binding.plc4x.canopen.ta.tapi.val.Value;

public class FilterValueCallback<T extends Value<?>> implements ValueCallback<Value<?>> {

  private final ValueCallback<T> delegate;
  private final int index;
  private final Class<T> type;

  public FilterValueCallback(ValueCallback<T> delegate, int index, Class<T> type) {
    this.delegate = delegate;
    this.index = index;
    this.type = type;
  }

  @Override
  public void accept(int index, Value<?> value) {
    if (this.index == 0) {
      return;
    }
    if (this.index == index && type.isInstance(value)) {
      delegate.accept(index, (T) value);
    }
  }

}
