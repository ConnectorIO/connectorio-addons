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
package org.connectorio.addons.startlevel.osgi;

import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;

/**
 * Ready markers related to OSGi modules and their services.
 */
public interface OsgiReadyMarker {

  String MARKER_TYPE = "osgi.service";

  static ReadyMarker serviceMarker(String id) {
    return new ReadyMarker(MARKER_TYPE, id);
  }

  static ReadyMarkerFilter serviceFilter(String id) {
    return new ReadyMarkerFilter().withType(MARKER_TYPE).withIdentifier(id);
  }

}
