/*
 * Copyright (C) 2019-2021 ConnectorIO sp. z o.o.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *     https://www.gnu.org/licenses/gpl-3.0.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.connectorio.addons.binding.bacnet.internal.handler.property;

import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.Polarity;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.Date;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.SignedInteger;
import com.serotonin.bacnet4j.type.primitive.Time;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetClientException;
import org.code_house.bacnet4j.wrapper.api.BacNetToJavaConverter;
import org.code_house.bacnet4j.wrapper.api.Property;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadPropertyTask implements Runnable, BacNetToJavaConverter<State> {

  private final Logger logger = LoggerFactory.getLogger(ReadPropertyTask.class);
  private final Supplier<CompletableFuture<BacNetClient>> client;
  private final ThingHandlerCallback callback;
  private final Property property;
  private final ChannelUID channelUID;

  public ReadPropertyTask(Supplier<CompletableFuture<BacNetClient>> client, ThingHandlerCallback callback, Property property, ChannelUID channelUID) {
    this.client = client;
    this.callback = callback;
    this.property = property;
    this.channelUID = channelUID;
  }

  @Override
  public void run() {
    CompletableFuture<BacNetClient> clientFuture = client.get();
    if (clientFuture.isDone() && !clientFuture.isCancelled() && !clientFuture.isCompletedExceptionally()) {
      try {
        Optional.ofNullable(clientFuture.get())
          .map(connection -> connection.getPropertyValue(property, this))
          .ifPresent(state -> {
            logger.debug("Requesting state {} for property {}", state, property);
            callback.stateUpdated(channelUID, state);
          });
      } catch (BacNetClientException e) {
        logger.warn("Could not read property {} value. Client reported an error", property, e);
      } catch (InterruptedException | ExecutionException e) {
        logger.debug("Could not complete operation", e);
      }
    }
  }

  @Override
  public State fromBacNet(Encodable encodable) {
    logger.debug("Mapping value {} of type {} for channel {}", encodable, encodable.getClass(), channelUID);
    if (encodable instanceof Null) {
      return UnDefType.NULL;
    } else if (encodable instanceof Real) {
      return new DecimalType(((Real) encodable).floatValue());
    } else if (encodable instanceof BinaryPV) {
      return BinaryPV.active.equals(encodable) ? OnOffType.ON : OnOffType.OFF;
    } else if (encodable instanceof Polarity) {
      return Polarity.normal.equals(encodable) ? OnOffType.ON : OnOffType.OFF;
    } else if (encodable instanceof UnsignedInteger) {
      return new DecimalType(((UnsignedInteger) encodable).intValue());
    } else if (encodable instanceof SignedInteger) {
      return new DecimalType(((SignedInteger) encodable).intValue());
    } else if (encodable instanceof Boolean) {
      return Boolean.TRUE == encodable ? OnOffType.ON : OnOffType.OFF;
    } else if (encodable instanceof Time) {
      Time time = (Time) encodable;
      // HH:mm:ss.SSSZ
      String millis = time.getHundredth() != 0 ? "." + time.getHundredth() : "";
      return new DateTimeType(time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + millis);
    } else if (encodable instanceof Date) {
      Date date = (Date) encodable;
      return new DateTimeType(date.calculateGC().toZonedDateTime());
    }
    logger.info("Received property value is currently not supported");
    return null;
  }
}
