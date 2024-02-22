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
package org.connectorio.addons.persistence.manager.internal;

import org.connectorio.addons.persistence.manager.PersistenceMarker;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Ready marker which translates custom start level into named ready marker.
 */
@Component(immediate = true)
public class PersistenceServiceReadyMarker implements ReadyTracker {

  private final ReadyService readService;

  @Activate
  public PersistenceServiceReadyMarker(@Reference ReadyService readyService) {
    this.readService = readyService;
    readyService.registerTracker(this, new ReadyMarkerFilter()
      .withType(PersistenceMarker.PERSISTENCE_START_LEVEL.getType())
      .withIdentifier(PersistenceMarker.PERSISTENCE_START_LEVEL.getIdentifier()));
  }

  @Override
  public void onReadyMarkerAdded(ReadyMarker readyMarker) {
    readService.markReady(PersistenceMarker.PERSISTENCE_SERVICES);
  }

  @Override
  public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    readService.markReady(PersistenceMarker.PERSISTENCE_SERVICES);
  }

}
