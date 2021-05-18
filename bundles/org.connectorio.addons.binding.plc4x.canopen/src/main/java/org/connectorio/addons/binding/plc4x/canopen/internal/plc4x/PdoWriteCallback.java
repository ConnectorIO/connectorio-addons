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

import java.util.function.BiConsumer;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PdoWriteCallback implements BiConsumer<PlcWriteResponse, Throwable> {

  private final Logger logger = LoggerFactory.getLogger(PdoWriteCallback.class);

  private final int nodeId;
  private final CANOpenService service;
  private final PlcValue value;

  PdoWriteCallback(int nodeId, CANOpenService service, PlcValue value) {
    this.nodeId = nodeId;
    this.service = service;
    this.value = value;
  }

  @Override
  public void accept(PlcWriteResponse response, Throwable error) {
    if (error != null) {
      logger.warn("Failed to dispatch PDO to node {}, service {}, cob {}, data {}", nodeId, service, Integer.toHexString(service.getMin() + nodeId), value, error);
      return;
    }

    logger.trace("Dispatched PDO node {}, service {}, cob {}, data {}", nodeId, service, Integer.toHexString(service.getMin() + nodeId), value);
  }

}