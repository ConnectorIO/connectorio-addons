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
package org.connectorio.addons.binding.bacnet.internal.handler.object;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.primitive.Null;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.code_house.bacnet4j.wrapper.api.BacNetClient;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.api.JavaToBacNetConverter;
import org.code_house.bacnet4j.wrapper.api.Priorities;
import org.code_house.bacnet4j.wrapper.api.Priority;
import org.code_house.bacnet4j.wrapper.api.Type;
import org.connectorio.addons.binding.bacnet.ObjectHandler;
import org.connectorio.addons.binding.bacnet.internal.BACnetBindingConstants;
import org.connectorio.addons.binding.bacnet.internal.command.PrioritizedCommand;
import org.connectorio.addons.binding.bacnet.internal.command.ResetCommand;
import org.connectorio.addons.binding.bacnet.internal.config.ChannelConfig;
import org.connectorio.addons.binding.bacnet.internal.config.ObjectConfig;
import org.connectorio.addons.binding.bacnet.internal.handler.channel.converter.CompositeConverter;
import org.connectorio.addons.binding.bacnet.internal.handler.object.task.Names;
import org.connectorio.addons.binding.bacnet.internal.handler.source.BACnetObjectsSampler;
import org.connectorio.addons.binding.bacnet.internal.handler.source.BACnetPropertySampler;
import org.connectorio.addons.binding.bacnet.internal.handler.source.BACnetSamplerComposer;
import org.connectorio.addons.binding.bacnet.internal.handler.source.ChannelCallback;
import org.connectorio.addons.binding.bacnet.internal.handler.source.SamplerCallback;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.connectorio.addons.binding.source.SourceFactory;
import org.connectorio.addons.binding.source.sampling.SamplingSource;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BACnetObjectThingHandler<T extends BACnetObject, B extends BACnetDeviceBridgeHandler<?, ?>, C extends ObjectConfig>
  extends BasePollingThingHandler<B, C> implements ObjectHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Type type;
  private final SourceFactory sourceFactory;
  private BacNetObject object;
  private Priority writePriority;
  private SamplingSource<BACnetPropertySampler> source;

  public BACnetObjectThingHandler(Thing thing, Type type, SourceFactory sourceFactory) {
    super(thing);
    this.type = type;
    this.sourceFactory = sourceFactory;
  }

  @Override
  public void initialize() {
    Device device = getBridgeHandler().map(b -> b.getDevice()).orElse(null);
    int instance = getThingConfig().map(c -> c.instance).orElseThrow(() -> new IllegalStateException("Undefined instance number"));
    writePriority = getThingConfig().map(c -> c.writePriority).flatMap(Priorities::get).orElse(null);

    if (device != null) {
      this.object = new BacNetObject(device, instance, type);

      if (writePriority != null) {
        logger.info("Using custom write priority {} for property {}", writePriority, object);
      }

      getBridgeHandler().ifPresent(bridge -> {
        bridge.getClient().thenAccept(client -> {

          this.source = sourceFactory.sampling(scheduler, new BACnetSamplerComposer(client));
          for (Channel channel : thing.getChannels()) {
            Long pollInterval = channelPollInterval(channel.getUID());

            Consumer<Encodable> consumer = new SamplerCallback(CompositeConverter.INSTANCE, new ChannelCallback(getCallback(), channel));
            source.add(pollInterval, channel.getUID().getAsString(), new BACnetObjectsSampler(client, object, Names.dashed(channel.getUID().getId()), consumer));
          }
          this.source.start();
          updateStatus(ThingStatus.ONLINE);
        });
      });
    } else {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Link object to device");
    }
  }

  private Long channelPollInterval(ChannelUID channelUID) {
    return Optional.ofNullable(getThing().getChannel(channelUID).getConfiguration())
      .map(cfg -> cfg.as(ChannelConfig.class))
      .map(cfg -> cfg.refreshInterval)
      .filter(interval -> interval != 0)
      .orElseGet(this::getRefreshInterval);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BACnetBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    String attribute = Names.dashed(channelUID.getId());
    logger.debug("Handle command {} for channel {} and property {}", command, channelUID, object);

    if (!getBridgeHandler().isPresent()) {
      logger.error("Handler is not attached to an bridge or bridge initialization failed!");
      return;
    }

    final CompletableFuture<BacNetClient> clientFuture = getBridgeHandler().get().getClient();

    if (command == RefreshType.REFRESH) {
      clientFuture.thenAccept(client -> {
        source.request(new BACnetObjectsSampler(client, object, attribute, new SamplerCallback(
            CompositeConverter.INSTANCE, new ChannelCallback(getCallback(), thing.getChannel(channelUID)))));
      });
    } else if (command instanceof ResetCommand) {
      ResetCommand reset = (ResetCommand) command;
      JavaToBacNetConverter<Object> converter = (value) -> {
        logger.trace("Issuing NULL command to BACnet value to channel {}/property {}", channelUID,
            object);
        return Null.instance;
      };
      if (reset.getPriority() == null) {
        if (writePriority == null) {
          logger.debug("Submitting NULL value for channel {} to {}", channelUID, object);
          clientFuture.join().setObjectPropertyValue(object, attribute, null, converter);
        } else {
          logger.debug("Submitting NULL value for channel {} to {} with priority {}", channelUID,
              object, writePriority);
          clientFuture.join().setObjectPropertyValue(object, attribute, null, converter, writePriority);
        }
      } else {
        logger.debug("Submitting NULL value for channel {} to {} with custom reset priority {}", channelUID,
            object, writePriority);
        clientFuture.join().setObjectPropertyValue(object, attribute, null, converter, reset.getPriority());
      }
    } else {
      Priority priority = writePriority;
      if (command instanceof PrioritizedCommand) {
        PrioritizedCommand prioritizedCmd = (PrioritizedCommand) command;
        priority = prioritizedCmd.getPriority();
        command = prioritizedCmd.getCommand();
      }
      JavaToBacNetConverter<Command> converter = (value) -> {
        Encodable encodable = BACnetValueConverter.openHabTypeToBacNetValue(type.getBacNetType(), value);
        logger.trace("Command have been converter to BACnet value {} of type {}", encodable, encodable.getClass());
        return encodable;
      };
      if (priority == null) {
        logger.debug("Submitting write {} from channel {} to {}", channelUID, command, object);
        clientFuture.join().setObjectPropertyValue(object, attribute, command, converter);
      } else {
        logger.debug("Submitting write {} from channel {} to {} with priority {}", channelUID, command,
            object, priority);
        clientFuture.join().setObjectPropertyValue(object, attribute, command, converter, priority);
      }
      logger.debug("Command {} for property {} executed successfully", command, object);
    }
  }

  private void poll(long cycle, Set<ChannelUID> channels) {
    if (!getBridgeHandler().isPresent()) {
      logger.error("Handler is not attached to an bridge or bridge initialization failed!");
      return;
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    if (source != null) {
      source.stop();
    }
  }

  @Override
  public Optional<BacNetClient> getClient() {
    try {
      return Optional.ofNullable(getBridgeHandler().get().getClient().get());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  @Override
  public BacNetObject getObject() {
    return object;
  }
}
