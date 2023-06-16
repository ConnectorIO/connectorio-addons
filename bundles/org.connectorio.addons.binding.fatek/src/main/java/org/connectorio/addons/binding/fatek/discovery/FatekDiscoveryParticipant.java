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
package org.connectorio.addons.binding.fatek.discovery;

import java.util.Optional;
import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * Fatek Discovery Participant can be used to provide custom thing type mapping for connected PLCs.
 *
 * By default, binding will attempt to use generic thing handler which may not be valid for all cases.
 * Use this extension point to handle devices which have a manufacturer specific data blocks or
 * blocks which have additional meaning, exceeding standard description.
 *
 * Registered participants are sorted using service.ranking priority. One with higher priority will be
 * used first. Next handlers are called only if current participant returns empty result.
 */
public interface FatekDiscoveryParticipant {

  /**
   * Attempt to discover device behind provided connection.
   *
   * Return of that method (discovery result) can be later used to create specific Thing Handler
   * which will provide own mapping logic for bits and pieces available via given connection.
   *
   * @param bridgeUID Bridge identifier.
   * @param stationNumber PLC station number.
   * @param timeToLive Time to live for discovery result.
   * @param connection Connection to PLC.
   * @return An empty optional if no custom mapping is possible with this participant, otherwise a
   * specific discovery result which should be used to create a new thing after acceptance by end
   * user.
   */
  Optional<DiscoveryResult> discover(ThingUID bridgeUID, short stationNumber, long timeToLive, FaconConnection connection);

}
