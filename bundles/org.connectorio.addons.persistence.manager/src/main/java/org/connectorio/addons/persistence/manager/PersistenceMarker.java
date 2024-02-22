/*
 * Copyright (C) 2024-2024 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.persistence.manager;

import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.StartLevelService;

/**
 * {@link ReadyMarker} types by persistence related trackers.
 */
public interface PersistenceMarker {

  /**
   * Dedicated start level which indicate that all necessary persistence services are active.
   */
  ReadyMarker PERSISTENCE_START_LEVEL = new ReadyMarker(StartLevelService.STARTLEVEL_MARKER_TYPE, "11");

  /**
   * Named ready marker used to inform that all persistence services are active, same as above start level.
   */
  ReadyMarker PERSISTENCE_SERVICES = new ReadyMarker("co7io-persistence", "services");

  /**
   * Indicates that persistence services have been configured by persistence manager extension.
   */
  ReadyMarker PERSISTENCE_CONFIGURE = new ReadyMarker("co7io-persistence", "configure");



}
