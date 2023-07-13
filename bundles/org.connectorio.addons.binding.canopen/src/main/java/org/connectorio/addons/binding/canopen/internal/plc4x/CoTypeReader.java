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
package org.connectorio.addons.binding.canopen.internal.plc4x;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;

class CoTypeReader<T> extends AbstractReader<PlcReadResponse, T> {

  private final CANOpenDataType type;

  public CoTypeReader(String field, CANOpenDataType type) {
    super(field);
    this.type = type;
  }

  @Override
  protected T extract(PlcReadResponse response, String field) {
    switch (type) {
      case BOOLEAN:
        return (T) response.getBoolean(field);
      case UNSIGNED8:
      case UNSIGNED16:
      case INTEGER8:
      case INTEGER16:
        return (T) response.getShort(field);
      case UNSIGNED24:
      case UNSIGNED32:
      case INTEGER24:
      case INTEGER32:
        return (T) response.getInteger(field);
      case UNSIGNED40:
      case UNSIGNED48:
      case UNSIGNED56:
      case UNSIGNED64:
      case INTEGER40:
      case INTEGER48:
      case INTEGER56:
      case INTEGER64:
        return (T) response.getLong(field);
      case REAL32:
        return (T) response.getDouble(field);
      case REAL64:
        return (T) response.getBigDecimal(field);
      case OCTET_STRING:
      case VISIBLE_STRING:
      case UNICODE_STRING:
        return (T) response.getString(field);
    }
    return (T) new CoRecordReader(field).apply(response);
  }

}