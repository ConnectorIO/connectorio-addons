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

import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.canopen.readwrite.CANOpenDataType;
import org.apache.plc4x.java.canopen.tag.CANOpenSDOTag;
import org.connectorio.addons.binding.canopen.api.CoSdoAware;

public class DefaultSdoAware extends DefaultNodeAware implements CoSdoAware {

  public DefaultSdoAware(DefaultConnection connection, int nodeId) {
    super(connection, nodeId);
  }

  @Override
  public CompletableFuture<byte[]> read(short index, short subindex) {
    CANOpenSDOTag fieldQuery = new CANOpenSDOTag(nodeId, index, subindex, CANOpenDataType.RECORD);
    return connection.connection.readRequestBuilder().addTag("record-data", fieldQuery).build()
      .execute().thenApply(new CoRecordReader("record-data"));
  }

  @Override
  public <T> CompletableFuture<T> read(short index, short subIndex, CANOpenDataType type) {
    CANOpenSDOTag fieldQuery = new CANOpenSDOTag(nodeId, index, subIndex, type);
    return connection.connection.readRequestBuilder().addTag("typed-data", fieldQuery).build()
      .execute().thenApply(new CoTypeReader<>("typed-data", type));
  }

}
