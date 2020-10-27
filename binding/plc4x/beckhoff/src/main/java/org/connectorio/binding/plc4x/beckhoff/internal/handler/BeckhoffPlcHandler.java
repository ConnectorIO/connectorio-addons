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
package org.connectorio.binding.plc4x.beckhoff.internal.handler;

import org.apache.plc4x.java.api.PlcConnection;
import org.connectorio.binding.plc4x.beckhoff.internal.BeckhoffBindingConstants;
import org.connectorio.binding.plc4x.beckhoff.internal.config.BeckhoffBridgeConfiguration;
import org.connectorio.binding.plc4x.shared.handler.base.PollingPlc4xThingHandler;
import org.openhab.core.thing.Thing;

public class BeckhoffPlcHandler extends PollingPlc4xThingHandler<PlcConnection, BeckhoffBridgeHandler<PlcConnection, ?>,
  BeckhoffBridgeConfiguration> {

  public BeckhoffPlcHandler(Thing thing) {
    super(thing);
  }

  @Override
  protected Long getDefaultPollingInterval() {
    return BeckhoffBindingConstants.DEFAULT_REFRESH_INTERVAL;
  }

}
