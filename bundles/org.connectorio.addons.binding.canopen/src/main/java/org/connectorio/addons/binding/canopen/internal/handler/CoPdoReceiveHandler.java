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
package org.connectorio.addons.binding.canopen.internal.handler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.ReadBuffer;
import org.connectorio.addons.binding.canopen.api.CoNode;
import org.connectorio.addons.binding.canopen.api.CoSubscription;
import org.connectorio.addons.binding.canopen.config.ReceivePdoConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.util.HexUtils;

/**
 * Handler which receives PDO data and maps it back to channel states.
 */
public class CoPdoReceiveHandler extends AbstractPdoHandler<ReceivePdoConfig> implements Consumer<byte[]>, Runnable {

  private final AtomicReference<Long> updated = new AtomicReference<>();
  private CoSubscription subscription;
  private ScheduledFuture<?> timeoutCheck;

  public CoPdoReceiveHandler(Thing thing) {
    super(thing, ReceivePdoConfig.class);
  }

  @Override
  protected void doInitialize(CoNode node) {
    node.subscribe(config.service, this).whenComplete((result, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
        return;
      }
      this.subscription = result;
    });

    Long refreshInterval = getRefreshInterval();
    if (refreshInterval > 0L) {
      timeoutCheck = scheduler.scheduleAtFixedRate(this, 0, refreshInterval, TimeUnit.MILLISECONDS);
    } else {
      // assume it is online as soon as parent is online
      updateStatus(ThingStatus.ONLINE);
    }
  }

  private Long getRefreshInterval() {
    Long refreshInterval = config.refreshInterval;
    if (refreshInterval == null) {
      refreshInterval = 0L;
    }
    return refreshInterval;
  }

  @Override
  public void run() {
    Long timestamp = updated.get();
    if (timestamp != null) {
      if (timestamp + getRefreshInterval() < System.currentTimeMillis()) {
        ZonedDateTime instant = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Last update received at " + instant);
      } else {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
          updateStatus(ThingStatus.ONLINE);
        }
      }
    } else {
      // initialize check
      updated.set(System.currentTimeMillis());
    }
  }

  @Override
  public void dispose() {
    if (timeoutCheck != null) {
      timeoutCheck.cancel(true);
      timeoutCheck = null;
    }
    if (subscription != null) {
      subscription.unsubscribe();
      subscription = null;
    }

    super.dispose();
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    logger.debug("Receive PDO handler is not supposed to send anything back!");
  }

  @Override
  public void accept(byte[] bytes) {
    updated.set(System.currentTimeMillis());

    ReadBuffer buffer = new ReadBuffer(bytes, true);
    int offset = 0;
    for (Entry<CANOpenDataType, Channel> entry : template) {
      try {
        CANOpenDataType type = entry.getKey();
        getCallback().stateUpdated(entry.getValue().getUID(), createState(buffer, type));
        offset += type.getNumBits();
      } catch (ParseException e) {
        logger.error("Failed to parse response {}. Failure after {} bit", HexUtils.bytesToHex(bytes), offset, e);
        break;
      }
    }
  }

  private State createState(ReadBuffer buffer, CANOpenDataType type) throws ParseException {
    switch (type) {
      case BOOLEAN:
        return buffer.readBit() ? OnOffType.ON : OnOffType.OFF;
      case UNSIGNED8:
      case UNSIGNED16:
      case INTEGER8:
      case INTEGER16:
        return new DecimalType(buffer.readShort(type.getNumBits()));
      case UNSIGNED24:
      case UNSIGNED32:
      case INTEGER24:
      case INTEGER32:
        return new DecimalType(buffer.readInt(type.getNumBits()));
      case UNSIGNED40:
      case UNSIGNED48:
      case UNSIGNED56:
      case UNSIGNED64:
      case INTEGER40:
      case INTEGER48:
      case INTEGER56:
      case INTEGER64:
        return new DecimalType(buffer.readLong(type.getNumBits()));
      case REAL32:
        return new DecimalType(buffer.readDouble(type.getNumBits()));
      case REAL64:
        return new DecimalType(buffer.readBigDecimal(type.getNumBits()));
      case OCTET_STRING:
      case VISIBLE_STRING:
      case UNICODE_STRING:
        return new StringType(buffer.readString(type.getNumBits(), "UTF-8"));
    }

    throw new IllegalArgumentException("Unsupported record type " + type);
  }

}
