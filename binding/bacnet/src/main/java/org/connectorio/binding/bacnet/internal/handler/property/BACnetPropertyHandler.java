/*
 * Copyright (C) 2019-2020 ConnectorIO sp. z o.o.
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
package org.connectorio.binding.bacnet.internal.handler.property;

import com.serotonin.bacnet4j.obj.BACnetObject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.api.Property;
import org.code_house.bacnet4j.wrapper.api.Type;
import org.connectorio.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.binding.bacnet.internal.config.ChannelConfig;
import org.connectorio.binding.bacnet.internal.config.ObjectConfig;
import org.connectorio.binding.base.handler.polling.common.BasePollingThingHandler;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BACnetPropertyHandler<T extends BACnetObject, B extends BACnetDeviceBridgeHandler<?, ?>, C extends ObjectConfig>
  extends BasePollingThingHandler<B, C> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Type type;
  private Property property;
  private Future<?> reader;

  public BACnetPropertyHandler(Thing thing, Type type) {
    super(thing);
    this.type = type;
  }

  @Override
  public void initialize() {
    Device device = getBridgeHandler().map(b -> b.getDevice()).orElse(null);
    int instance = getThingConfig().map(c -> c.instance).orElseThrow(() -> new IllegalStateException("Undefined instance number"));

    if (device != null) {
      this.property = new Property(device, instance, type);

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
      scheduler.execute(new Runnable() {
        @Override
        public void run() {
          logger.debug("Dispatching command {} to property {}", command, property);
          client.join().setPropertyValue(property, command, (value) -> {
            logger.trace("Command {} have been converter to BACnet value {} of type {}", command, value, value.getClass());
            return BACnetValueConverter.openHabTypeToBacNetValue(type.getBacNetType(), value);
          });
          logger.debug("Command {} for property {} executed successfully", command, property);
        }
      });
    }
  }

  @Override
  public void channelLinked(ChannelUID channelUID) {
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
  public void channelUnlinked(ChannelUID channelUID) {
    logger.info("BACnet channel unlinked {}", channelUID);
    if (reader != null) {
      reader.cancel(true);
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
