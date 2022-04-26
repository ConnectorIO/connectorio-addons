/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.smartme.internal.handler;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.connectorio.addons.binding.smartme.SmartMeBindingConstants;
import org.connectorio.addons.binding.smartme.internal.config.DeviceConfig;
import org.connectorio.addons.binding.smartme.v1.ApiClient;
import org.connectorio.addons.binding.smartme.v1.ApiException;
import org.connectorio.addons.binding.smartme.v1.client.DevicesApi;
import org.connectorio.addons.binding.smartme.v1.client.model.Device;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartMeDeviceThingHandler extends BasePollingThingHandler<SmartMeCloudThingHandler, DeviceConfig> {

  private final Logger logger = LoggerFactory.getLogger(SmartMeDeviceThingHandler.class);
  private ApiClient client;
  private String deviceId;
  private Device device;
  private ScheduledFuture<?> reader;

  public SmartMeDeviceThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return SmartMeBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

  @Override
  public void initialize() {
    DeviceConfig config = getThingConfig().orElse(null);

    if (config == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing configuration");
      return;
    }

    if (config.deviceId == null || config.deviceId.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing device id");
      return;
    }
    this.deviceId = config.deviceId;

    ApiClient client = getClient().orElse(null);
    if (client == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "API client not found");
      return;
    }

    try {
      DevicesApi devicesApi = new DevicesApi(client);
      this.device = devicesApi.devicesGet_0(deviceId);
      updateStatus(ThingStatus.ONLINE);

      Long cycleTime = config.refreshInterval == null ? getRefreshInterval() : SmartMeBindingConstants.DEFAULT_REFRESH_INTERVAL;
      reader = scheduler.scheduleAtFixedRate(new ReadTask(devicesApi, deviceId, thing, getCallback()), 0,
        cycleTime, TimeUnit.MILLISECONDS);
    } catch (ApiException e) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Could not retrieve device information " + e.getMessage());
      logger.error("Could not retrieve device, API call failed", e);
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public void dispose() {
    super.dispose();
    if (reader != null && reader.isCancelled()) {
      reader.cancel(true);
    }
    if (device != null) {
      device = null;
    }
    if (deviceId != null) {
      deviceId = null;
    }
  }

  public Optional<ApiClient> getClient() {
    return getBridgeHandler().flatMap(SmartMeCloudThingHandler::getClient);
  }

}
