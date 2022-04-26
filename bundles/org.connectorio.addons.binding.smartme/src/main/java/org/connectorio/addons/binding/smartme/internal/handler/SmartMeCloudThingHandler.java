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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.connectorio.addons.binding.handler.polling.common.BasePollingBridgeHandler;
import org.connectorio.addons.binding.smartme.SmartMeBindingConstants;
import org.connectorio.addons.binding.smartme.internal.client.AuthInterceptor;
import org.connectorio.addons.binding.smartme.internal.config.CloudConfig;
import org.connectorio.addons.binding.smartme.v1.ApiClient;
import org.connectorio.addons.binding.smartme.v1.ApiException;
import org.connectorio.addons.binding.smartme.v1.client.UserApi;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartMeCloudThingHandler extends BasePollingBridgeHandler<CloudConfig> {

  private final Logger logger = LoggerFactory.getLogger(SmartMeCloudThingHandler.class);
  private ApiClient client;

  public SmartMeCloudThingHandler(Bridge bridge) {
    super(bridge);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return SmartMeBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

  @Override
  public void initialize() {
    CloudConfig cloudConfig = getBridgeConfig().orElse(null);
    if (cloudConfig == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing configuration");
      return;
    }

    if (cloudConfig.username == null || cloudConfig.username.trim().isEmpty() ||
        cloudConfig.password == null || cloudConfig.password.trim().isEmpty()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing username and/or password settings");
      return;
    }

    String address = SmartMeBindingConstants.DEFAULT_CLOUD_URI;
    if (cloudConfig.url != null && cloudConfig.url.trim().isEmpty() && !cloudConfig.url.equals(address)) {
      address = cloudConfig.url;
    }

    try {
      URL url = new URL(address);
      this.client = new ApiClient()
        .setRequestInterceptor(new AuthInterceptor(cloudConfig.username, cloudConfig.password))
        .setScheme(url.getProtocol())
        .setHost(url.getHost())
        .setPort(url.getPort())
        .setBasePath(url.getPath());

      try {
        new UserApi(client).userGet();
        updateStatus(ThingStatus.ONLINE);
      } catch (ApiException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Could not retrieve user information " + e.getMessage());
        logger.error("Could not retrieve user information, API call failed", e);
      }
    } catch (MalformedURLException e) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Wrong address " + e.getMessage());
      logger.error("Failed to construct API client", e);
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  public void dispose() {
    super.dispose();
    if (client != null) {
      client = null;
    }
  }

  public Optional<ApiClient> getClient() {
    return Optional.ofNullable(client);
  }
}
