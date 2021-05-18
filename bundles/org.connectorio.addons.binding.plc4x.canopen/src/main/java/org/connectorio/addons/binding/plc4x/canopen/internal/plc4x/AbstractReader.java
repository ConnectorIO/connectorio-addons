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
package org.connectorio.addons.binding.plc4x.canopen.internal.plc4x;

import java.util.function.Function;
import org.apache.plc4x.java.api.messages.PlcFieldResponse;
import org.apache.plc4x.java.api.model.PlcField;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very basic handler of field responses. Bound to single field and its response code.
 *
 * @param <T> Type of result for coming implementations.
 */
public abstract class AbstractReader<T extends PlcFieldResponse, R> implements Function<T, R> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final String field;

  public AbstractReader(String field) {
    this.field = field;
  }

  @Override
  public R apply(T response) {
    if (response.getResponseCode(field) == PlcResponseCode.OK) {
      return extract(response, field);
    }

    PlcField fieldObject = response.getRequest().getField(this.field);
    logger.debug("Failed request {}. Field {} response code is {}", response.getRequest(), this.field, response.getResponseCode(this.field));
    throw new IllegalStateException("Field " + this.field + " (" +  fieldObject + ") retrieval failed. Reported code: " + response.getResponseCode(this.field));
  }

  protected abstract R extract(T response, String field);
}
