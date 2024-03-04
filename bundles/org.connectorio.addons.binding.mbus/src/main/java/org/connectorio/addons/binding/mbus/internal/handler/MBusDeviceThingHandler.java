/*
 * Copyright (C) 2023-2023 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.mbus.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.connectorio.addons.binding.mbus.config.BridgeConfig;
import org.connectorio.addons.binding.mbus.config.ChannelConfig;
import org.connectorio.addons.binding.mbus.config.DeviceConfig;
import org.connectorio.addons.binding.mbus.internal.handler.converter.Converter;
import org.connectorio.addons.binding.mbus.internal.handler.source.ChannelKey;
import org.connectorio.addons.binding.mbus.internal.handler.source.MBusChannelCallback;
import org.connectorio.addons.binding.mbus.internal.handler.source.MBusPrimaryAddressSampler;
import org.connectorio.addons.binding.mbus.internal.handler.source.MBusRecordCallback;
import org.connectorio.addons.binding.mbus.internal.handler.source.MBusSampler;
import org.connectorio.addons.binding.mbus.internal.handler.source.MBusSecondaryAddressSampler;
import org.connectorio.addons.binding.source.SourceFactory;
import org.connectorio.addons.binding.source.sampling.SamplingSource;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.SecondaryAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBusDeviceThingHandler extends BasePollingThingHandler<MBusBridgeHandler<BridgeConfig>, DeviceConfig> {
  private final Logger logger = LoggerFactory.getLogger(MBusDeviceThingHandler.class);

  private final Map<ChannelUID, ChannelKey> channelMapping = new ConcurrentHashMap<>();
  private final Converter converter;
  private final SourceFactory sourceFactory;
  private MBusConnection connection;
  private SamplingSource<MBusSampler> source;
  private Integer address;
  private SecondaryAddress secondaryAddress;

  public MBusDeviceThingHandler(Thing thing, Converter converter, SourceFactory sourceFactory) {
    super(thing);
    this.converter = converter;
    this.sourceFactory = sourceFactory;
  }

  @Override
  public void initialize() {
    CompletableFuture<MBusConnection> bridgeConnection = getBridgeHandler()
      .map(MBusBridgeHandler::getConnection)
      .orElse(null);

    if (bridgeConnection == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Could not find bridge");
      return;
    }

    DeviceConfig config = getConfigAs(DeviceConfig.class);
    if (config.address == null && config.getSecondaryAddress() == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing device address and/or secondary address components");
      return;
    }

    bridgeConnection.whenComplete((result, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge supplied connection is not available");
        return;
      }
      this.connection = result;
      this.address = config.address;
      this.secondaryAddress = config.getSecondaryAddress();

      this.source = sourceFactory.sampling(scheduler);
      Map<ChannelKey, Consumer<DataRecord>> callbacks = new HashMap<>();
      for (Channel channel : getThing().getChannels()) {
        ChannelConfig channelConfig = channel.getConfiguration().as(ChannelConfig.class);
        ChannelKey channelKey = new ChannelKey(channelConfig.dib, channelConfig.vib);
        callbacks.put(channelKey, new MBusRecordCallback(converter, new MBusChannelCallback(getCallback(), channel)));
        if (channelMapping.containsValue(channelKey)) {
          updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Detected duplicate mapping " + channelKey + ", channel key must be unique.");
          return;
        }
        channelMapping.put(channel.getUID(), channelKey);
      }

      if (secondaryAddress != null) {
        source.add(config.refreshInterval, thing.getUID().getAsString(), new MBusSecondaryAddressSampler(connection, secondaryAddress, callbacks));
      } else {
        source.add(config.refreshInterval, thing.getUID().getAsString(), new MBusPrimaryAddressSampler(connection, address, callbacks));
      }

      source.start();

      updateStatus(ThingStatus.ONLINE);
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (channelMapping.containsKey(channelUID)) {
      if (RefreshType.REFRESH.equals(command) && this.source != null) {
        ChannelKey channelKey = channelMapping.get(channelUID);
        // read channel status independently of scheduled tasks
        Channel channel = thing.getChannel(channelUID);
        if (address != null) {
          source.request(new MBusPrimaryAddressSampler(connection, address, Map.of(
            channelKey, new MBusRecordCallback(converter, new MBusChannelCallback(getCallback(), channel))
          )));
        } else if (secondaryAddress != null) {
          source.request(new MBusSecondaryAddressSampler(connection, secondaryAddress, Map.of(
            channelKey, new MBusRecordCallback(converter, new MBusChannelCallback(getCallback(), channel))
          )));
        } else {
          logger.warn("Could not determine target M-Bus device address, ignoring REFRESH call");
        }
        return;
      }
    }
    logger.warn("Unsupported command {} for channel {}, M-Bus binding is read only integration", command, channelUID);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return null;
  }

}
