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
package org.connectorio.addons.binding.relayweblog.internal.handler;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import javax.measure.quantity.Volume;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants;
import org.connectorio.addons.binding.relayweblog.client.WeblogClient;
import org.connectorio.addons.binding.relayweblog.client.dto.MeterReading;
import org.connectorio.addons.binding.relayweblog.internal.config.MeterConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractMeterThingHandler<B extends WeblogBridgeHandler, C extends MeterConfig> extends BasePollingThingHandler<B, C> implements Runnable {

  private final static DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .append(ISO_LOCAL_TIME)
    .toFormatter();

  private final static Map<String, MappingEntry> MAPPING = Map.of(
    "kW", new MappingEntry(Power.class, MetricPrefix.KILO(tec.uom.se.unit.Units.WATT)),
    "kWh", new MappingEntry(Energy.class, Units.KILOWATT_HOUR),
    "m^3", new MappingEntry(Volume.class, tec.uom.se.unit.Units.CUBIC_METRE),
    "m^3/h", new MappingEntry(VolumetricFlowRate.class, Units.CUBICMETRE_PER_HOUR),
    "C", new MappingEntry(Temperature.class, tec.uom.se.unit.Units.CELSIUS),
    "h", new MappingEntry(Time.class, Units.HOUR),
    "s", new MappingEntry(Time.class, Units.SECOND)
  );

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private ScheduledFuture<?> future;
  protected C config;

  protected AbstractMeterThingHandler(Thing thing) {
    super(thing);
  }

  protected abstract Map<String, String> properties(Thing thing, List<MeterReading> readings);

  protected abstract List<MeterReading> readOut(WeblogClient client, C config);

  @Override
  public void initialize() {
    if (!getThingConfig().isPresent()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Required configuration not found");
      return;
    }

    config = getThingConfig().get();
    if (config.id == null || config.id.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Required configuration property 'id' not found.");
      return;
    }

    getBridgeHandler().flatMap(WeblogBridgeHandler::getClient)
      .ifPresent(client -> {
        try {
          List<MeterReading> readings = readOut(client, config);
          if (getThing().getChannels().isEmpty()) {
            // fetch channels from weblog
            ThingBuilder thing = editThing();
            thing.withProperties(properties(getThing(), readings));

            thing.withChannels(lookupChannels(readings));
            updateThing(thing.build());
          }

          Long cycleTime = getRefreshInterval();
          future = scheduler.scheduleAtFixedRate(this, 1000, cycleTime, TimeUnit.MILLISECONDS);
          updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
          logger.error("Could not initialize meter handler", e);
        }
      });
  }

  private List<Channel> lookupChannels(List<MeterReading> readings) {
    List<Channel> channels = new ArrayList<>();
    for (int index = 0, readingsSize = readings.size(); index < readingsSize; index++) {
      if (!acceptable(readings.get(index))) {
        continue;
      }

      MeterReading reading = readings.get(index);
      String name = reading.getName().trim();
      String unit = reading.getUnit().trim();

      if (DATE_AND_TIME_FIELD.equalsIgnoreCase(name)) {
        Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), "datetime#" + index))
          .withType(RelayWeblogBindingConstants.DATE_TIME_CHANNEL_TYPE)
          .withAcceptedItemType(CoreItemFactory.DATETIME)
          .withKind(ChannelKind.STATE)
          .withLabel(name).build();
        channels.add(channel);
      } else if (ERROR_FLAGS_FIELD.equalsIgnoreCase(name)) {
        // meter status
        Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), "status#" + index))
          .withType(RelayWeblogBindingConstants.STATUS_CHANNEL_TYPE)
          .withAcceptedItemType(CoreItemFactory.NUMBER)
          .withKind(ChannelKind.STATE)
          .withLabel(name).build();
        channels.add(channel);
      } else {
        if (!unit.isEmpty() && !name.isEmpty()) {
          MappingEntry entry = MAPPING.get(unit);
          if (entry == null) {
            logger.info("Unsupported reading {}. Ignoring", reading);
            continue;
          }

          Channel channel = ChannelBuilder
            .create(new ChannelUID(getThing().getUID(), entry.type.getSimpleName().toLowerCase() + "#" + index))
            .withType(RelayWeblogBindingConstants.channelType(entry.type))
            .withAcceptedItemType(CoreItemFactory.NUMBER + ":" + entry.type.getSimpleName())
            .withKind(ChannelKind.STATE)
            .withLabel(name).build();
          channels.add(channel);
        } else {
          logger.info("Anonymous reading {}. Ignoring.", reading);
        }
      }
    }
    return channels;
  }

  protected abstract boolean acceptable(MeterReading reading);

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    // TODO handle REFRESH command, no other command can be handled cause its read only interface
  }

  @Override
  public void dispose() {
    if (future != null) {
      try {
        future.cancel(true);
      } catch (Exception e) {
        logger.info("Error while shutting down handler", e);
      }
    }
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return RelayWeblogBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  public static Optional<MeterReading> find(List<MeterReading> readings, String name) {
    return readings.stream()
      .filter(reading -> name.equalsIgnoreCase(reading.getName()))
      .findFirst();
  }

  @Override
  public void run() {
    getBridgeHandler().flatMap(WeblogBridgeHandler::getClient)
      .ifPresent(client -> {
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
          return;
        }

        try {
          C config = getThingConfig().get();
          List<MeterReading> readings = readOut(client, config);

          for (int index = 0, readingsSize = readings.size(); index < readingsSize; index++) {
            MeterReading reading = readings.get(index);
            String name = reading.getName().trim();
            String unit = reading.getUnit().trim();
            String value = reading.getValue().trim();

            if (DATE_AND_TIME_FIELD.equalsIgnoreCase(name)) {
              ChannelUID uid = new ChannelUID(getThing().getUID(), "datetime#" + index);
              callback.stateUpdated(uid, parseDate(value));
            } else if (ERROR_FLAGS_FIELD.equalsIgnoreCase(name)) {
              ChannelUID uid = new ChannelUID(getThing().getUID(), "status#" + index);
              callback.stateUpdated(uid, new DecimalType(Long.parseLong(value)));
            } else {
              if (!unit.isEmpty() && !name.isEmpty()) {
                MappingEntry entry = MAPPING.get(unit);
                if (entry == null) {
                  logger.info("Unsupported reading {}. Ignoring", reading);
                  continue;
                }
                ChannelUID uid = new ChannelUID(getThing().getUID(), entry.type.getSimpleName().toLowerCase() + "#" + index);
                callback.stateUpdated(uid, map(entry, value));
              }
            }
          }
        } catch (Exception e) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
      });
  }

  private QuantityType<?> map(MappingEntry entry, String value) {
    BigDecimal number = DecimalType.valueOf(value).toBigDecimal();
    return new QuantityType<>(number, entry.unit);
  }

  private State parseDate(String date) {
    LocalDateTime localDateTime = LocalDateTime.parse(date, DATE_TIME_FORMATTER);
    ZonedDateTime dateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
    return new DateTimeType(dateTime);
  }

  static class MappingEntry {
    final Class<? extends Quantity<?>> type;
    final Unit<?> unit;

    MappingEntry(Class<? extends Quantity<?>> type, Unit<?> unit) {
      this.type = type;
      this.unit = unit;
    }
  }
}
