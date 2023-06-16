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
package org.connectorio.addons.binding.askoheat.internal.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.ClientBuilder;
import org.connectorio.addons.binding.askoheat.client.AskoheatClient;
import org.connectorio.addons.binding.askoheat.client.DirectAskoheatClient;
import org.connectorio.addons.binding.askoheat.client.dto.CommandBlock;
import org.connectorio.addons.binding.askoheat.client.dto.ParameterBlock;
import org.connectorio.addons.binding.askoheat.client.dto.ValueBlock;
import org.connectorio.addons.binding.askoheat.client.dto.fullstatus.DeviceInfo;
import org.connectorio.addons.binding.askoheat.client.dto.fullstatus.FullStatus;
import org.connectorio.addons.binding.askoheat.config.AskoheatConfig;
import org.connectorio.addons.binding.askoheat.internal.AskoheatBindingConstants;
import org.connectorio.addons.binding.handler.polling.common.BasePollingBridgeHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

public class AskoheatBridgeHandler extends BasePollingBridgeHandler<AskoheatConfig> {

  private final List<ScheduledFuture<?>> tasks = new ArrayList<>();
  private final ClientBuilder clientBuilder;
  private AskoheatClient client;

  public AskoheatBridgeHandler(Bridge bridge, ClientBuilder clientBuilder) {
    super(bridge);
    this.clientBuilder = clientBuilder;
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return AskoheatBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  @Override
  public void initialize() {
    if (!getBridgeConfig().isPresent()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing required configuration");
      return;
    }

    this.client = getBridgeConfig().map(cfg -> new DirectAskoheatClient(clientBuilder, cfg.address)).orElse(null);
    if (client == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing address information.");
      return;
    }

    try {
      Map<String, String> properties = new LinkedHashMap<>();
      FullStatus status = client.getFullStatus();
      if (status != null && status.getDeviceInfo() != null) {
        DeviceInfo info = status.getDeviceInfo();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, info.getSerialNumber());
        properties.put(Thing.PROPERTY_MODEL_ID, info.getArticleNumber());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, info.getHardwareVersion());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, info.getSoftwareVersion());
        properties.put(Thing.PROPERTY_VENDOR, "ASKOMA AG");
        properties.put("type", info.getType());
      }
      getThing().setProperties(properties);

      updateStatus(ThingStatus.ONLINE);
    } catch (Exception e) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not communicate with device: " + e.getMessage());
      return;
    }

    tasks.add(scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        ParameterBlock params = client.getParameterBlock();
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "heater1"), QuantityType.valueOf(params.getHeater1(), Units.WATT));
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "heater2"), QuantityType.valueOf(params.getHeater2(), Units.WATT));
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "heater3"), QuantityType.valueOf(params.getHeater3(), Units.WATT));
      }
    }, 1000L, getRefreshInterval(), TimeUnit.MILLISECONDS));
    tasks.add(scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        ValueBlock params = client.getValueBlock();
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "status"), new DecimalType(params.getStatus()));
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "load"), QuantityType.valueOf(params.getLoad(), Units.WATT));
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "temperatureLimit"), QuantityType.valueOf(params.getTemperatureLimit(), tech.units.indriya.unit.Units.CELSIUS));
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "temperatureSensor0"), QuantityType.valueOf(params.getTemperatureSensor0(), tech.units.indriya.unit.Units.CELSIUS));
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "maximumTemperature"), QuantityType.valueOf(params.getMaximumTemperature(), tech.units.indriya.unit.Units.CELSIUS));
      }
    }, 1000L, getRefreshInterval(), TimeUnit.MILLISECONDS));
    tasks.add(scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        CommandBlock params = client.getCommandBlock();
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "step"), new DecimalType(params.getStep()));
        getCallback().stateUpdated(new ChannelUID(getThing().getUID(), "gridPower"), QuantityType.valueOf(params.getGridPower(), Units.WATT));
      }
    }, 1000L, getRefreshInterval(), TimeUnit.MILLISECONDS));
  }

  @Override
  public void dispose() {
    if (!tasks.isEmpty()) {
      for (ScheduledFuture<?> future : tasks) {
        future.cancel(false);
      }
    }
    tasks.clear();
    client = null;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if ("gridPower".equals(channelUID.getId()) && (command instanceof DecimalType || command instanceof QuantityType)) {
      CommandBlock block = new CommandBlock();
      if (command instanceof DecimalType) {
        block.setGridPower(((DecimalType) command).intValue());
      } else {
        block.setGridPower(((QuantityType<?>) command).intValue());
      }
      client.sendCommandBlock(block);
    }
  }

}
