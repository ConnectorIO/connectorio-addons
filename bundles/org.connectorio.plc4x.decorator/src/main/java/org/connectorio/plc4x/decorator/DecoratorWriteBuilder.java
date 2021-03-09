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
package org.connectorio.plc4x.decorator;

import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteRequest.Builder;

public class DecoratorWriteBuilder implements PlcWriteRequest.Builder {

  private final Builder delegate;
  private final WriteDecorator decorator;

  public DecoratorWriteBuilder(Builder delegate, WriteDecorator decorator) {
    this.delegate = delegate;
    this.decorator = decorator;
  }

  @Override
  public PlcWriteRequest build() {
    return decorator.decorateWriteRequest(delegate.build());
  }

  @Override
  public Builder addItem(String name, String fieldQuery, Object... values) {
    delegate.addItem(name, fieldQuery, values);
    return this;
  }

}
