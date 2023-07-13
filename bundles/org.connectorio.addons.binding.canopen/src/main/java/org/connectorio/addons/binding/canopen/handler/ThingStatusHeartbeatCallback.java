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
package org.connectorio.addons.binding.canopen.handler;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

/**
 * Heartbeat status callback which simply calls {@link ThingHandlerCallback}.
 */
public class ThingStatusHeartbeatCallback implements HeartbeatCallback {

  private final Thing thing;
  private final ThingHandlerCallback callback;

  public ThingStatusHeartbeatCallback(Thing thing, ThingHandlerCallback callback) {
    this.thing = thing;
    this.callback = callback;
  }

  @Override
  public void updateStatus(ThingStatus status, ThingStatusDetail details, String message) {
    ThingStatusInfoBuilder statusBuilder = ThingStatusInfoBuilder.create(status, details);
    ThingStatusInfo statusInfo = statusBuilder.withDescription(message).build();

    update(statusInfo);
  }

  @Override
  public void updateStatus(ThingStatus status) {
    update(ThingStatusInfoBuilder.create(status).build());
  }

  private void update(ThingStatusInfo status) {
    callback.statusUpdated(thing, status);

  }
}
