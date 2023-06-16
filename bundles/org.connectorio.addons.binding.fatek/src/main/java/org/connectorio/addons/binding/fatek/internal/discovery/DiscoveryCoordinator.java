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
package org.connectorio.addons.binding.fatek.internal.discovery;

import org.connectorio.addons.binding.fatek.transport.FaconConnection;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * Central entry point to discovery sub-framework which allows to customize device to handler mapping.
 *
 * Given that Fatek PLC might be embedded in different use cases it allows to handle cases where a
 * PLC with specific configuration can be mapped in different, then usual, way into runtime.
 */
public interface DiscoveryCoordinator {

  /**
   * Attempt to identify and map connected PLC into a thing handler.
   *
   * Return of that method (discovery result) can be later used to create specific Thing Handler
   * which will provide own mapping logic for bits and pieces specific to this connection.
   *
   * @param bridgeUID Identification of interface which provided message.
   * @param stationNumber Station number of PLC
   * @param connection Connection to a PLC.
   * @param timeToLive Time to live for discovery result.
   * @return Null if discovery of thing handler have failed, otherwise a complete discovery result
   * ready to be passed to upper discovery layers.
   */
  DiscoveryResult discover(ThingUID bridgeUID, short stationNumber, FaconConnection connection, long timeToLive);


}
