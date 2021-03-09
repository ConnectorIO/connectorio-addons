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
package org.connectorio.addons.binding.plc4x.canopen.internal.plc4x;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.canopen.field.CANOpenSDOField;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.connectorio.addons.binding.plc4x.canopen.api.CoSDOAware;

public class DefaultSDOAware extends DefaultNodeAware implements CoSDOAware {

  public DefaultSDOAware(DefaultConnection connection, int nodeId) {
    super(connection, nodeId);
  }

  @Override
  public CompletableFuture<byte[]> read(short index, short subindex) {
    CANOpenSDOField fieldQuery = new CANOpenSDOField(nodeId, index, subindex, CANOpenDataType.RECORD);
    return connection.connection.readRequestBuilder().addItem("data", fieldQuery).build()
      .execute().thenApply(new ResultReader("data"));
  }

  @Override
  public <T> CompletableFuture<T> read(short index, short subindex, CANOpenDataType type) {
    CANOpenSDOField fieldQuery = new CANOpenSDOField(nodeId, index, subindex, type);
    return connection.connection.readRequestBuilder().addItem("data", fieldQuery).build()
      .execute().thenApply(new ReadResponseFunction<>(type));
  }

  static class ReadResponseFunction<T> implements Function<PlcReadResponse, T> {

    private final CANOpenDataType type;

    public ReadResponseFunction(CANOpenDataType type) {
      this.type = type;
    }

    @Override
    public T apply(PlcReadResponse plcReadResponse) {
      if (plcReadResponse.getResponseCode("data") != PlcResponseCode.OK) {
        throw new IllegalArgumentException("Could not fetch data, error code: " + plcReadResponse.getResponseCode("data"));
      }

      switch (type) {
        case BOOLEAN:
          return (T) plcReadResponse.getBoolean("data");
        case UNSIGNED8:
        case UNSIGNED16:
        case INTEGER8:
        case INTEGER16:
          return (T) plcReadResponse.getShort("data");
        case UNSIGNED24:
        case UNSIGNED32:
        case INTEGER24:
        case INTEGER32:
          return (T) plcReadResponse.getInteger("data");
        case UNSIGNED40:
        case UNSIGNED48:
        case UNSIGNED56:
        case UNSIGNED64:
        case INTEGER40:
        case INTEGER48:
        case INTEGER56:
        case INTEGER64:
          return (T) plcReadResponse.getLong("data");
        case REAL32:
          return (T) plcReadResponse.getDouble("data");
        case REAL64:
          return (T) plcReadResponse.getBigDecimal("data");
        case OCTET_STRING:
        case VISIBLE_STRING:
        case UNICODE_STRING:
          return (T) plcReadResponse.getString("data");
      }
      return (T) ResultReader.getBytes(plcReadResponse, "data");
    }
  }

}
