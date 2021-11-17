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

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import javax.ws.rs.client.ClientBuilder;
import org.connectorio.addons.binding.handler.polling.common.BasePollingBridgeHandler;
import org.connectorio.addons.binding.relayweblog.RelayWeblogBindingConstants;
import org.connectorio.addons.binding.relayweblog.WeblogHandler;
import org.connectorio.addons.binding.relayweblog.client.CachingWeblogClient;
import org.connectorio.addons.binding.relayweblog.client.DirectWeblogClient;
import org.connectorio.addons.binding.relayweblog.client.SubMeterWeblogClient;
import org.connectorio.addons.binding.relayweblog.client.WeblogClient;
import org.connectorio.addons.binding.relayweblog.internal.config.WeblogConfig;
import org.connectorio.addons.binding.relayweblog.internal.discovery.WeblogMeterDiscoveryService;
import org.connectorio.addons.binding.relayweblog.internal.discovery.WeblogSubMeterDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeblogBridgeHandler extends BasePollingBridgeHandler<WeblogConfig> implements WeblogHandler {

  private final ClientBuilder clientBuilder;
  private Logger logger = LoggerFactory.getLogger(WeblogBridgeHandler.class);
  private WeblogClient client;

  public WeblogBridgeHandler(Bridge thing, ClientBuilder clientBuilder) {
    super(thing);
    this.clientBuilder = clientBuilder;
  }

  @Override
  public void initialize() {
    Optional<WeblogConfig> config = getBridgeConfig();
    if (!config.isPresent()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing required configuration");
      return;
    }

    WeblogConfig cfg = config.get();
    if (cfg.address.isBlank()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing address parameter");
      return;
    }
    if (cfg.password.isBlank()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Missing password parameter");
      return;
    }

    try {
      String address = cfg.address.endsWith("/") ? cfg.address.substring(0, cfg.address.length() - 1) : cfg.address;
      client = new CachingWeblogClient(new SubMeterWeblogClient(new DirectWeblogClient(clientBuilder, address, cfg.password)));
      updateStatus(ThingStatus.ONLINE);
    } catch (Exception e) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
      logger.error("Could not complete initialization of Weblog bridge", e);
    }
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  public Optional<WeblogClient> getClient() {
    return Optional.ofNullable(client);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return RelayWeblogBindingConstants.DEFAULT_POLLING_INTERVAL;
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Arrays.asList(WeblogMeterDiscoveryService.class, WeblogSubMeterDiscoveryService.class);
  }

}
