/*
 * Copyright (C) 2019-2024 ConnectorIO Sp. z o.o.
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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Simple console addon which prints resolved dependencies for ready markers.
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class StartLevelDebugCommand extends AbstractConsoleCommandExtension {

    private final ReadyService readyService;

    @Activate
    public StartLevelDebugCommand(@Reference ReadyService readyService) {
        super("co7io-start-level-debug", "Debug start level and ready markers");
        this.readyService = readyService;
    }

    @Override
    public void execute(String[] args, Console console) {
        Set<ReadyMarker> ready = new LinkedHashSet<>();
        ReadyTracker readyTracker = new ReadyTracker() {
            @Override
            public void onReadyMarkerAdded(ReadyMarker readyMarker) {
                ready.add(readyMarker);
            }

            @Override
            public void onReadyMarkerRemoved(ReadyMarker readyMarker) {}
        };
        try {
            readyService.registerTracker(readyTracker);
        } finally {
            readyService.unregisterTracker(readyTracker);
        }

        Map<ReadyTracker, ReadyMarkerFilter> trackers = trackers(console);
        for (ReadyTracker tracker : trackers.keySet()) {
            ReadyMarkerFilter filter = trackers.get(tracker);
            Set<ReadyMarker> resolved = ready.stream().filter(filter::apply).collect(Collectors.toSet());
            console.println("Tracker " + tracker);
            console.println("  Filter " + inspect(filter));
            if (!resolved.isEmpty()) {
                console.println("  Resolved:");
                for (ReadyMarker marker : resolved) {
                    console.println("  - " + marker);
                }
            }
        }
    }

    private String inspect(ReadyMarkerFilter filter) {
        Class<? extends ReadyMarkerFilter> filterClass = filter.getClass();
        try {
            Field type = filterClass.getDeclaredField("type");
            if (!type.isAccessible()) {
                type.setAccessible(true);
            }
            Field identifier = filterClass.getDeclaredField("identifier");
            if (!identifier.isAccessible()) {
                identifier.setAccessible(true);
            }
            return type.get(filter) + "=" + identifier.get(filter);
        } catch (Exception e) {
            return filter.toString();
        }
    }

    private Map<ReadyTracker, ReadyMarkerFilter> trackers(Console console) {
        try {
            Field trackers = readyService.getClass().getDeclaredField("trackers");
            if (!trackers.isAccessible()) {
                trackers.setAccessible(true);
            }
            Object value = trackers.get(readyService);
            if (value instanceof Map) {
                return (Map<ReadyTracker, ReadyMarkerFilter>) value;
            }

            console.println("Unsupported tracker content " + value);
            return Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList("co7io-start-level-debug");
    }
}
