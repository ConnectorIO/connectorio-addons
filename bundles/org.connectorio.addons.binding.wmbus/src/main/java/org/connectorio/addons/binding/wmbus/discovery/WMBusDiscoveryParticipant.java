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
package org.connectorio.addons.binding.wmbus.discovery;

import java.util.Optional;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;
import org.openmuc.jmbus.wireless.WMBusMessage;

/**
 * WM-Bus Discovery Participant can be used to provide custom thing type mapping for specific WM-Bus
 * devices.
 *
 * By default, binding will attempt to use standard encoding scheme which may not be valid for all cases.
 * Use this extension point to handle devices which rely on manufacturer specific data blocks.
 *
 * Registered participants are sorted using service.ranking priority. One with higher priority will be
 * used first. Next handlers are called only if current participant returns empty result.
 */
public interface WMBusDiscoveryParticipant {

  /**
   * Attempt to identify specific device from received WM-Bus message.
   *
   * Return of that method (discovery result) can be later used to create specific Thing Handler
   * which will provide own mapping logic for bits and pieces received in message.
   *
   * @param bridgeUID Bridge identifier.
   * @param timeToLive Time to live for discovery result.
   * @param message Received message.
   * @return An empty optional if no custom mapping is possible with this participant, otherwise a
   * specific discovery result which should be used to create a new thing after acceptance by end user.
   */
  Optional<DiscoveryResult> discover(ThingUID bridgeUID, long timeToLive, WMBusMessage message);

}
