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
package org.connectorio.addons.binding.plc4x.canopen.internal.discovery;

import java.util.function.Consumer;
import org.connectorio.addons.binding.plc4x.canopen.internal.CANopenBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

public class FallbackDiscoveryResultCallback implements DiscoveryCallback {

  private final Consumer<DiscoveryResult> callback;
  private final ThingUID bridgeUID;

  public FallbackDiscoveryResultCallback(Consumer<DiscoveryResult> callback, ThingUID bridgeUID) {
    this.callback = callback;
    this.bridgeUID = bridgeUID;
  }

  @Override
  public void thingAvailable(int node, DiscoveryResult result) {
    if (result != null) {
      callback.accept(result);
      return;
    }

    // Discovery participants did not bring any information about discovery result, meaning that we have pretty
    // much a generic CANopen node which can be read via PDO/SDO requests. This is a fallback to create a generic thing.
    DiscoveryResult genericResult = DiscoveryResultBuilder
      .create(new ThingUID(CANopenBindingConstants.NODE_BRIDGE_TYPE, bridgeUID, String.valueOf(node)))
      .withLabel("Generic CANopen node " + node)
      .withRepresentationProperty("nodeId")
      .withBridge(bridgeUID)
      .withProperty("nodeId", node)
      .build();
    callback.accept(genericResult);
  }
}
