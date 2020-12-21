/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.binding.plc4x.beckhoff.internal.handler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.config.BeckhoffAmsAdsConfiguration;
import org.connectorio.addons.binding.plc4x.beckhoff.internal.config.BeckhoffBridgeConfiguration;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top level type unifying handling of Beckhoff bridges.
 *
 * @author Lukasz Dywicki - Initial contribution
 */
public abstract class BeckhoffBridgeHandler<T extends PlcConnection, C extends BeckhoffBridgeConfiguration> extends
  PollingPlc4xBridgeHandler<T, C> {

  private final Logger logger = LoggerFactory.getLogger(BeckhoffBridgeHandler.class);
  private CompletableFuture<T> initializer = new CompletableFuture<>();;

  public BeckhoffBridgeHandler(Bridge thing) {
    super(thing);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
  }

  @Override
  public void initialize() {
    updateStatus(ThingStatus.UNKNOWN);

    if (getBridge() == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Please attach this bridge handler to AMS ADS network bridge.");
      return;
    }

    if (getBridge().getHandler() != null && !(getBridge().getHandler() instanceof BeckhoffAmsAdsBridgeHandler)) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Unknown bridge handler used.");
      return;
    }

    Optional<BeckhoffAmsAdsConfiguration> config = ((BeckhoffAmsAdsBridgeHandler) getBridge().getHandler()).getBridgeConfig();
    if (!config.isPresent()) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "AMS ADS network bridge is not configured yet.");
      return;
    }

    Runnable connectionTask = createInitializer(config.get(), initializer);
    scheduler.submit(connectionTask);
  }

  protected abstract Runnable createInitializer(BeckhoffAmsAdsConfiguration amsAds, CompletableFuture<T> initializer);

  @Override
  protected CompletableFuture<T> getPlcConnection() {
      return initializer;
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BeckhoffBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
