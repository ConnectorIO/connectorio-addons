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
package org.connectorio.addons.binding.mbus.internal.discovery;

import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;
import org.openmuc.jmbus.VariableDataStructure;

/**
 * Central entry point to discovery sub-framework which allows to customize device to handler mapping.
 */
public interface DiscoveryCoordinator {

  /**
   * Attempt to map received M-Bus message into a thing handler.
   *
   * Return of that method (discovery result) can be later used to create specific Thing Handler
   * which will provide own mapping logic for bits and pieces received in message.
   * Created result should point only to secondary address.
   * A primary address might append by a caller of this interface, if it's known to it.
   *
   * @param bridgeUID Identification of interface which provided message.
   * @param timeToLive Time to live for discovery result.
   * @param message Received M-Bus telegram.
   * @return Null if discovery of thing handler have failed, otherwise a complete discovery result
   * ready to be passed to upper discovery layers.
   */
  DiscoveryResult discover(ThingUID bridgeUID, long timeToLive, VariableDataStructure message);


}
