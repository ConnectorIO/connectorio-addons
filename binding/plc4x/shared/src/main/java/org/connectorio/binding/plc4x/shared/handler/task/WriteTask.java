package org.connectorio.binding.plc4x.shared.handler.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.connectorio.binding.plc4x.shared.config.CommonChannelConfiguration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.types.Command;
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

    PlcResponseCode object = response.getResponseCode(channelConfig.field);
    switch (object) {
      case OK:
        logger.info("Value {} set to {}", channelConfig.field, response.getField(channelConfig.field));
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