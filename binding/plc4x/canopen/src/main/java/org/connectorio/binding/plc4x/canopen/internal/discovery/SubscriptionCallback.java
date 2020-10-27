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
package org.connectorio.binding.plc4x.canopen.internal.discovery;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.value.PlcValue;
import org.connectorio.binding.plc4x.canopen.discovery.CANopenDiscoveryParticipant;
import org.connectorio.binding.plc4x.canopen.internal.handler.CANOpenSocketCANBridgeHandler;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.ThingUID;

class SubscriptionCallback implements Consumer<PlcSubscriptionEvent> {

  private final String fieldName;
  private final CANOpenSocketCANBridgeHandler handler;
  private final PlcConnection connection;
  private final DiscoveryCallback callback;

  public SubscriptionCallback(String fieldName, CANOpenSocketCANBridgeHandler handler, PlcConnection connection, DiscoveryCallback callback) {
    this.fieldName = fieldName;
    this.handler = handler;
    this.connection = connection;
    this.callback = callback;
  }

  @Override
  public void accept(PlcSubscriptionEvent subscriptionEvent) {
    final PlcValue value = subscriptionEvent.getPlcValue(fieldName);
    if (value != null) {
      final Map<String, ? extends PlcValue> struct = value.getStruct();
      final Integer node = Optional.ofNullable(struct.get("node")).map(PlcValue::getInt).orElse(0);
      final Integer state = Optional.ofNullable(struct.get("state")).map(PlcValue::getInt).orElse(0);

      if (0x05 == state && node != 0) { // operational
        DiscoveryResult discoveryResult = null;
        ThingUID bridgeUID = handler.getThing().getUID();
        for (CANopenDiscoveryParticipant caNopenDiscoveryParticipant : handler.getParticipants()) {
          discoveryResult = caNopenDiscoveryParticipant.nodeDiscovered(connection, bridgeUID, node);
          if (discoveryResult != null) {
            break;
          }
        }

        callback.thingAvailable(node, discoveryResult);
      }
    }
  }

}
