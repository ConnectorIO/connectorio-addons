/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
package org.connectorio.addons.startlevel.shell.internal;

import java.util.Arrays;
import java.util.List;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.openhab.core.service.StartLevelService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which gets start level information.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class StartLevelCommand extends AbstractConsoleCommandExtension {

  private final ReadyService readyService;

  @Activate
  public StartLevelCommand(@Reference ReadyService readyService) {
    super("co7io-start-level", "Show persistence statistics for items");
    this.readyService = readyService;
  }

  @Override
  public void execute(String[] args, Console console) {
    ReadyTracker readyTracker = new ReadyTracker() {
      @Override
      public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        console.println("Added " + readyMarker);
      }

      @Override
      public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        console.println("Removed " + readyMarker);
      }
    };
    try {
      readyService.registerTracker(readyTracker, new ReadyMarkerFilter().withType(StartLevelService.STARTLEVEL_MARKER_TYPE));
    } finally {
      readyService.unregisterTracker(readyTracker);
    }
  }

  @Override
  public List<String> getUsages() {
    return Arrays.asList("co7io-start-level");
  }

}
