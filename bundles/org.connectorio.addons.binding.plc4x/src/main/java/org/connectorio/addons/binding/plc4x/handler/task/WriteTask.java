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
package org.connectorio.addons.binding.plc4x.handler.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.connectorio.addons.binding.plc4x.config.CommonChannelConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteTask implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(WriteTask.class);
  private final PlcWriteRequest request;
  private final Channel channel;
  private final CommonChannelConfiguration channelConfig;

  public WriteTask(PlcConnection connection, Channel channel, Command value) {
    this.channel = channel;
    this.channelConfig = channel.getConfiguration()
      .as(CommonChannelConfiguration.class);

    this.request = connection.writeRequestBuilder()
      .addItem(getUID(), channelConfig.field, fromOpenhab(value))
      .build();
  }

  @Override
  public void run() {
    logger.info("Running task to write field {}", channelConfig.field);
    CompletableFuture<? extends PlcWriteResponse> execute = request.execute();
    try {
      // we should be able to fetch data within short time period, if not then it is a
      // transmission problem and we should not block this task any more.
      execute.whenComplete(this::handle)
        .get();
    } catch (InterruptedException | ExecutionException e) {
      logger.warn("Could not write data to PLC", e);
    }
  }

  private void handle(PlcWriteResponse response, Throwable throwable) {
    if (throwable != null) {
      logger.info("Could not read field {}, {}", channelConfig.field, throwable);
      return;
    }

    PlcResponseCode object = response.getResponseCode(getUID());
    switch (object) {
      case OK:
        logger.debug("Value {} set to {}", channelConfig.field, response.getField(getUID()));
        break;
      case NOT_FOUND:
        logger.warn("Block {} no found", channelConfig.field);
        break;
      case ACCESS_DENIED:
        logger.warn("Access to field {} denied", channelConfig.field);
        break;
      case INVALID_ADDRESS:
        logger.warn("Invalid address specification {}", channelConfig.field);
        break;
      case INVALID_DATATYPE:
        logger.warn("Wrong data type for field {}", channelConfig.field);
        break;
      case INTERNAL_ERROR:
        logger.warn("Could not process update of {} - internal error", channelConfig.field);
        break;
      case RESPONSE_PENDING:
        logger.warn("Waiting for update of value {}", channelConfig.field);
        break;
    }
  }

  private Object fromOpenhab(Command object) {
    if (object instanceof DecimalType) {
      return ((DecimalType) object).toBigDecimal();
    } else if (object instanceof OnOffType) {
      return object == OnOffType.ON;
    } else if (object instanceof OpenClosedType) {
      return object == OpenClosedType.OPEN;
    } else if (object instanceof DateTimeType) {
      return ((DateTimeType) object).getZonedDateTime();
    }
    return object.toString();
  }

  private String getUID() {
    return channel.getUID().getAsString();
  }
}