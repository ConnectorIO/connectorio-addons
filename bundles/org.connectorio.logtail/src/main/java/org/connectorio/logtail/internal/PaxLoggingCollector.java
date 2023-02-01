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
package org.connectorio.logtail.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedList;
import org.connectorio.logtail.Collector;
import org.connectorio.logtail.LogEntry;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of log event collector which sticks with Pax Logging and its {@link PaxAppender} SPI.
 *
 * Log entries are kept using "hard" references occupying memory, but with a size limitation.
 * There is a mere chance that collector will be causing memory issues. It will happen primarily when
 * large log statements will be stuck in buffer.
 * Currently, we do not consider that as a risk factor, given fairly low limit imposed on buffer size.
 */
@Component(immediate = true, configurationPid = PaxLoggingCollector.SERVICE_PID, property = {
  Constants.SERVICE_PID + "=" + PaxLoggingCollector.SERVICE_PID,
  "service.config.label=Log collector",
  "service.config.category=Software Diagnostics",
  "service.config.description.uri=connectorio:logtail",
})
public class PaxLoggingCollector implements Collector, PaxAppender, ManagedService {

  public final static String SERVICE_PID = "org.connectorio.logtail";
  public static final int DEFAULT_BUFFER_SIZE = 2000;

  private final Logger logger = LoggerFactory.getLogger(PaxLoggingCollector.class);

  private LinkedList<LogEntry> events = new LinkedList<>();
  private int bufferSize = DEFAULT_BUFFER_SIZE;

  @Override
  public Iterable<LogEntry> getLogEntries() {
    return Collections.synchronizedList(events);
  }

  @Override
  public void doAppend(PaxLoggingEvent paxLoggingEvent) {
    events.addFirst(new PaxLogEntry(
      paxLoggingEvent.getTimeStamp(),
      paxLoggingEvent.getLevel().toString(),
      paxLoggingEvent.getLoggerName(),
      paxLoggingEvent.getMessage()
    ));;
    if (events.size() > bufferSize) {
      events.removeLast();
    }
  }

  @Override
  public void updated(Dictionary<String, ?> dictionary) throws ConfigurationException {
    Object option = null;
    if ((option = dictionary.get("bufferSize")) != null) {
      if (option instanceof Number) {
        bufferSize = ((Number) option).intValue();
        return;
      } else {
        try {
          bufferSize = Integer.parseInt(option.toString());
          return;
        } catch (NumberFormatException e) {
          logger.warn("Could not parse value {} as an number, reverting to default buffer limit ({})", option, DEFAULT_BUFFER_SIZE);
        }
      }
    }
    bufferSize = DEFAULT_BUFFER_SIZE;
  }

}
