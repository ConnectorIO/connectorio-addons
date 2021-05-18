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
package org.connectorio.plc4x.decorator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.model.PlcField;
import org.apache.plc4x.java.api.value.PlcValue;

public class DecoratorWriteRequest implements PlcWriteRequest {

  private final PlcWriteRequest delegate;
  private final WriteDecorator decorator;

  public DecoratorWriteRequest(PlcWriteRequest delegate, WriteDecorator decorator) {
    this.delegate = delegate;
    this.decorator = decorator;
  }

  @Override
  public CompletableFuture<? extends PlcWriteResponse> execute() {
    return decorator.decorateWriteResponse(this, delegate.execute());
  }

  @Override
  public int getNumberOfValues(String name) {
    return delegate.getNumberOfValues(name);
  }

  @Override
  public PlcValue getPlcValue(String name) {
    return delegate.getPlcValue(name);
  }

  @Override
  public int getNumberOfFields() {
    return delegate.getNumberOfFields();
  }

  @Override
  public LinkedHashSet<String> getFieldNames() {
    return delegate.getFieldNames();
  }

  @Override
  public PlcField getField(String name) {
    return delegate.getField(name);
  }

  @Override
  public List<PlcField> getFields() {
    return delegate.getFields();
  }

  public String toString() {
    Map<String, String> fields = new HashMap<>();
    for (String name : getFieldNames()) {
      if (fields.put(name, getField(name) + ":" + getPlcValue(name)) != null) {
        throw new IllegalStateException("Duplicate key");
      }
    }
    return "DecoratorWriteRequest [" + delegate + ": " + fields + "]";
  }

}
