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
package org.connectorio.addons.binding.fatek.internal.handler;

import java.util.concurrent.CompletableFuture;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.connectorio.addons.binding.handler.polling.common.BasePollingThingHandler;
import org.connectorio.addons.binding.fatek.config.BridgeConfig;
import org.connectorio.addons.binding.fatek.config.DeviceConfig;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

public class FatekPlcThingHandler extends BasePollingThingHandler<FatekBridgeHandler<BridgeConfig>, DeviceConfig> {

  private FaconConnection connection;
  private Integer stationNumber;

  public FatekPlcThingHandler(Thing thing) {
    super(thing);
  }

  @Override
  public void initialize() {
    CompletableFuture<FaconConnection> bridgeConnection = getBridgeHandler()
      .map(FatekBridgeHandler::getConnection)
      .orElse(null);

    if (bridgeConnection == null) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Could not find bridge");
      return;
    }

    DeviceConfig config = getConfigAs(DeviceConfig.class);
    if (config.stationNumber == 0) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid station number");
      return;
    }
    this.stationNumber = config.stationNumber;
    bridgeConnection.whenComplete((result, error) -> {
      if (error != null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge supplied connection is not available");
        return;
      }
      this.connection = result;
      updateStatus(ThingStatus.ONLINE);
    });
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {

  }

  @Override
  protected Long getDefaultPollingInterval() {
    return 1000L;
  }

}
