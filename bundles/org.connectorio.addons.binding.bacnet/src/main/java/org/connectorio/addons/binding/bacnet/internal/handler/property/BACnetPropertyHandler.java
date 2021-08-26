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

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.api.JavaToBacNetConverter;
import org.code_house.bacnet4j.wrapper.api.Property;
import org.code_house.bacnet4j.wrapper.api.Type;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.addons.binding.bacnet.internal.config.ChannelConfig;
import org.connectorio.addons.binding.bacnet.internal.config.ObjectConfig;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BACnetPropertyHandler<T extends BACnetObject, B extends BACnetDeviceBridgeHandler<?, ?>, C extends ObjectConfig>
  extends BasePollingThingHandler<B, C> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Type type;
  private Property property;
  private Integer writePriority;
  private Future<?> reader;

  public BACnetPropertyHandler(Thing thing, Type type) {
    super(thing);
    this.type = type;
  }

  @Override
  public void initialize() {
    Device device = getBridgeHandler().map(b -> b.getDevice()).orElse(null);
    int instance = getThingConfig().map(c -> c.instance).orElseThrow(() -> new IllegalStateException("Undefined instance number"));
    writePriority = getThingConfig().map(c -> c.writePriority).orElse(null);
    if (writePriority != null && (writePriority < 1 || writePriority > 16)) {
      throw new IllegalStateException("Invalid write priority value");
    }

    if (device != null) {
      this.property = new Property(device, instance, type);

      if (writePriority != null) {
        logger.info("Using custom write priority {} for property {}", writePriority, property);
      }

      getThing().getChannels().stream()
        .peek(channel -> poll(channel.getUID()))
        .collect(Collectors.toList());
      updateStatus(ThingStatus.ONLINE);
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Link object to device");
    }
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    logger.debug("Handle command {} for channel {} and property {}", channelUID, command, property);

    final CompletableFuture<BacNetClient> client = getBridgeHandler().flatMap(bridge -> bridge.getClient())
      .orElseThrow(() -> new IllegalArgumentException("BACnet client is not ready"));

    if (command == RefreshType.REFRESH) {
      scheduler.execute(new ReadPropertyTask(() -> client, getCallback(), property, channelUID));
    } else {
      JavaToBacNetConverter<Command> converter = (value) -> {
        Encodable encodable = BACnetValueConverter.openHabTypeToBacNetValue(type.getBacNetType(), value);
        logger.trace("Command {} have been converter to BACnet value {} of type {}", command, encodable, encodable.getClass());
        return encodable;
      };
      if (writePriority == null) {
        logger.debug("Submitting write {} from channel {} to {}", channelUID, command, property);
        client.join().setPropertyValue(property, command, converter);
      } else {
        logger.debug("Submitting write {} from channel {} to {} with priority {}", channelUID, command, property, writePriority);
        client.join().setPropertyValue(property, command, converter, writePriority);
      }
      logger.debug("Command {} for property {} executed successfully", command, property);
    }
  }

  private void poll(ChannelUID channelUID) {
    logger.info("BACnet channel linked {}", channelUID);
    Supplier<CompletableFuture<BacNetClient>> client = () -> getBridgeHandler().flatMap(bridge -> bridge.getClient()).orElse(
      // return failed future if client is not yet ready
      CompletableFuture.completedFuture(null)
    );

    long refreshInterval = Optional.ofNullable(getThing().getChannel(channelUID).getConfiguration())
      .map(cfg -> cfg.as(ChannelConfig.class))
      .map(cfg -> cfg.refreshInterval)
      .filter(interval -> interval != 0)
      .orElseGet(this::getRefreshInterval);

    if (property != null) {
      this.reader = scheduler.scheduleAtFixedRate(new ReadPropertyTask(client, getCallback(), property, channelUID),
        0, refreshInterval, TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    if (reader != null) {
      reader.cancel(true);
    }
  }

}
