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
package org.connectorio.addons.binding.amsads.internal.handler;

import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.addons.binding.amsads.AmsAdsBindingConstants;
import org.connectorio.addons.binding.amsads.internal.config.BridgeConfiguration;
import org.connectorio.addons.binding.plc4x.handler.base.PollingPlc4xThingHandler;
import org.openhab.core.thing.Thing;

public class AmsAdsPlcHandler extends PollingPlc4xThingHandler<PlcConnection, AbstractAmsAdsBridgeHandler<PlcConnection, ?>,
  BridgeConfiguration> {

  public AmsAdsPlcHandler(Thing thing) {
    super(thing);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return AmsAdsBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
