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
package org.connectorio.addons.binding.plc4x.canopen.internal.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.canopen.readwrite.types.CANOpenDataType;
import org.apache.plc4x.java.spi.generation.ParseException;
import org.apache.plc4x.java.spi.generation.WriteBuffer;
import org.apache.plc4x.java.spi.values.PlcSINT;
import org.apache.plc4x.java.spi.values.PlcValues;
import org.connectorio.addons.binding.plc4x.canopen.api.CoNode;
import org.connectorio.addons.binding.plc4x.canopen.config.CoPdoConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Handler which transmits selected channels via PDO to the bus.
 */
public class CoPdoTransmitHandler extends AbstractPdoHandler<CoPdoConfig> implements Runnable {

  private final Map<ChannelUID, Command> values = new ConcurrentHashMap<>();
  private CoNode node;

  public CoPdoTransmitHandler(Thing thing) {
    super(thing, CoPdoConfig.class);
  }

  @Override
  protected void doInitialize(CoNode node) {
    this.node = node;

    Long refreshInterval = config.refreshInterval;
    if (refreshInterval == 0) {
      refreshInterval = 60_000L;
    }

    scheduler.scheduleAtFixedRate(this, 0, refreshInterval, TimeUnit.MILLISECONDS);
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (!(command instanceof RefreshType)) {
      values.put(channelUID, command);
    }

    publish();
  }

  @Override
  public void run() {
    publish();
  }

  private void publish() {
    WriteBuffer buffer = new WriteBuffer(8, true);
    for (Entry<CANOpenDataType, Channel> entry : template) {
      ChannelUID uid = entry.getValue().getUID();
      CANOpenDataType type = entry.getKey();
      try {
        if (values.containsKey(uid)) {
          writeState(buffer, type, values.get(uid));
        } else {
          buffer.writeInt(type.getNumBits(), 0);
        }
      } catch (ParseException e) {
        logger.error("Failed to map channel {} state {} to PDO", uid, values.get(uid), e);
        break;
      }
    }

    this.node.getConnection().send(node.getNodeId(), config.service, toPlcValue(buffer.getData()));
  }

  private PlcValue toPlcValue(byte[] data) {
    return PlcValues.of(
      new PlcSINT(data.length > 0 ? data[0] : 0),
      new PlcSINT(data.length > 1 ? data[1] : 0),
      new PlcSINT(data.length > 2 ? data[2] : 0),
      new PlcSINT(data.length > 3 ? data[3] : 0),
      new PlcSINT(data.length > 4 ? data[4] : 0),
      new PlcSINT(data.length > 5 ? data[5] : 0),
      new PlcSINT(data.length > 6 ? data[6] : 0),
      new PlcSINT(data.length > 7 ? data[7] : 0)
    );
  }

  private void writeState(WriteBuffer buffer, CANOpenDataType type, Command command) throws ParseException {
    switch (type) {
      case BOOLEAN:
        if (command instanceof OnOffType) {
          buffer.writeBit(command == OnOffType.ON);
          return;
        }
        if (command instanceof OpenClosedType) {
          buffer.writeBit(command == OpenClosedType.OPEN);
          return;
        }
        break;
      case UNSIGNED8:
      case UNSIGNED16:
      case INTEGER8:
      case INTEGER16:
        if (command instanceof DecimalType) {
          DecimalType value = (DecimalType) command;
          buffer.writeShort(type.getNumBits(), value.toBigDecimal().shortValue());
          return;
        }
        break;
      case UNSIGNED24:
      case UNSIGNED32:
      case INTEGER24:
      case INTEGER32:
        if (command instanceof DecimalType) {
          DecimalType value = (DecimalType) command;
          buffer.writeInt(type.getNumBits(), value.toBigDecimal().intValue());
          return;
        }
        break;
      case UNSIGNED40:
      case UNSIGNED48:
      case UNSIGNED56:
      case UNSIGNED64:
      case INTEGER40:
      case INTEGER48:
      case INTEGER56:
      case INTEGER64:
        if (command instanceof DecimalType) {
          DecimalType value = (DecimalType) command;
          buffer.writeLong(type.getNumBits(), value.toBigDecimal().longValue());
          return;
        }
        break;
      case REAL32:
      case REAL64:
        if (command instanceof DecimalType) {
          DecimalType value = (DecimalType) command;
          buffer.writeBigDecimal(type.getNumBits(), value.toBigDecimal());
          return;
        }
        break;
      case OCTET_STRING:
      case VISIBLE_STRING:
      case UNICODE_STRING:
        if (command instanceof StringType) {
          StringType value = (StringType) command;
          buffer.writeString(type.getNumBits(), "UTF-8", value.toString());
          return;
        }
        break;
    }

    throw new IllegalArgumentException("Unsupported record type " + type + " and value " + command + " combination");
  }

}
