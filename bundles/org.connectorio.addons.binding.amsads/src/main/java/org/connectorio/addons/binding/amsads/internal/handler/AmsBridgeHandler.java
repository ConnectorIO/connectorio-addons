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
package org.connectorio.addons.binding.amsads.internal.handler;

import java.util.Collection;
import java.util.Collections;
import org.connectorio.addons.binding.handler.GenericBridgeHandler;
import org.connectorio.addons.binding.handler.GenericBridgeHandlerBase;
import org.connectorio.addons.binding.amsads.internal.config.AmsConfiguration;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsDeviceDiscoveryService;
import org.connectorio.addons.binding.amsads.internal.discovery.AmsAdsDiscoveryDriver;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmsBridgeHandler} is responsible for defining virtual address needed to communicate with
 * AMS/ADS devices.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public class AmsBridgeHandler extends GenericBridgeHandlerBase<AmsConfiguration>
  implements GenericBridgeHandler<AmsConfiguration> {

  private final Logger logger = LoggerFactory.getLogger(AmsBridgeHandler.class);
  private final AmsAdsDiscoveryDriver discoveryDriver;

  public AmsBridgeHandler(Bridge thing, AmsAdsDiscoveryDriver discoveryDriver) {
    super(thing);
    this.discoveryDriver = discoveryDriver;
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.ONLINE);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  public Collection<Class<? extends ThingHandlerService>> getServices() {
    return Collections.singleton(AmsAdsDeviceDiscoveryService.class);
  }

  public AmsAdsDiscoveryDriver getDiscoveryDriver() {
    return discoveryDriver;
  }

}
